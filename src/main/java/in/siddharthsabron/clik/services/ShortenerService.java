package in.siddharthsabron.clik.services;

import in.siddharthsabron.clik.models.links.ShortUrl;
import in.siddharthsabron.clik.repositories.ShortUrlRepository;
import in.siddharthsabron.clik.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import in.siddharthsabron.clik.models.authentications.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * NOTE:
 * ShortenerService: Provides the core business logic for shortening and
 * resolving URLs.
 *
 * NOTE:
 * Key Features:
 * - Handles URL shortening, including duplicate detection and concurrency
 * control.
 * - Resolves short URLs to their original long URLs.
 * - Uses pessimistic locking (SELECT ... FOR UPDATE) for concurrency safety.
 * - Employs a multi-threaded approach for asynchronous click count updates.
 * - Adheres to SOLID principles.
 */
@Service
public class ShortenerService {

  private static final Logger logger = LoggerFactory.getLogger(ShortenerService.class);

  @Autowired
  private ShortUrlRepository shortUrlRepository;

  @Autowired
  private UserRepository userRepository;

  private final AtomicInteger sequence = new AtomicInteger(0);
  private final long epoch = 1672531200000L; // Example epoch
  private static final String HASH_ALGORITHM = "SHA-256";

  private final ExecutorService executorService = Executors.newFixedThreadPool(10);

  private int getShardId(byte[] hash) {
    return Math.abs(hash[0] % 10);
  }

  /**
   * Shortens a URL for a registered user.
   *
   * @param longUrl The URL to shorten.
   * @param userId  The ID of the registered user.
   * @return The generated short code.
   * @throws IllegalArgumentException If the user is not found.
   */
  @Transactional
  public String shortenUrl(String longUrl, Long userId) {
    Optional<User> user = userRepository.findByUserId(userId);
    if (user.isEmpty()) {
      throw new IllegalArgumentException("User not found with ID: " + userId);
    }
    else{
      logger.info("Shortening URL for user: {}", user.get().getUserId());
    }
  
    return shortenUrlInternal(longUrl, user.orElse(null));
  }

     /**s
   * Shortens a URL anonymously (without user association).
   *
   * @param longUrl The URL to shorten.
   * @return The generated short code.
   */
  @Transactional
  public String shortenUrl(String longUrl) {
    return shortenUrlInternal(longUrl, null); // Pass null for the user
  }

  /**
   * Internal method to handle URL shortening, shared by both user and anonymous
   * cases.
   *
   * @param longUrl The URL to shorten.
   * @param user    The User object (can be null for anonymous links).
   * @return The generated short code.
   */
  private String shortenUrlInternal(String longUrl, User user) {
    byte[] longUrlHash = calculateHash(longUrl);
    int shardId = getShardId(longUrlHash);

    Optional<ShortUrl> existingLink = shortUrlRepository.findByShardIdAndLongUrlHashForUpdate(shardId, longUrlHash);

    if (existingLink.isPresent()) {
      return existingLink.get().getShortCode();
    }

    long internalId = generateInternalId(shardId);
    String shortCode = toBase62(internalId);

    ShortUrl newLink = new ShortUrl(internalId, shortCode, longUrl, longUrlHash, shardId);
    newLink.setUser(user); // Set the user (can be null)
    shortUrlRepository.save(newLink);
    return shortCode;
  }

  @Transactional(readOnly = true)
  public Optional<String> getLongUrl(String shortCode) {
    logger.info("Getting long URL for shortCode: {}", shortCode);
    // Convert shortCode to internalId (Base-62 decoding)
    long internalId;
    try {
      internalId = fromBase62(shortCode);
    } catch (IllegalArgumentException e) {
      return Optional.empty(); // Invalid shortCode format
    }

    // Use internalId for the database lookup
    logger.info("Looking up short URL with internalId: {}", internalId);
    return shortUrlRepository.findByInternalId(internalId)
        .map(shortUrl -> {
          // Increment click count asynchronously if needed
          logger.info("=====Incrementing click count for short URL: {}", shortUrl);
          incrementClickCountAsync(shortUrl.getShardId(), internalId);
          logger.info("=====Returning long URL: {}", shortUrl.getLongUrl());
          return shortUrl.getLongUrl();
        });
  }

  /**
   * Asynchronously increments the click count for a given short URL.
   * Uses a thread pool to avoid blocking the main request thread.
   *
   * @param shardId    The shard ID.
   * @param internalId The internal ID of the short URL.
   */
  private void incrementClickCountAsync(Integer shardId, Long internalId) {
    logger.info("=====Incrementing click count for shardId: {}, ===== internalId: {}", shardId, internalId);
    executorService.submit(() -> {
      try {
        int updatedRows = shortUrlRepository.incrementClickCount(shardId, internalId);
        if (updatedRows > 0) {
          logger.info("=====Successfully incremented click count for shardId: {}, internalId: {}", shardId, internalId);
        } else {
          logger.warn("=====No rows updated when incrementing click count for shardId: {}, internalId: {}", shardId,
              internalId);
        }
      } catch (Exception e) {
        logger.error("=====Error incrementing click count for shardId: {}, internalId: {}", shardId, internalId, e);
      }
    });
  }

  /**
   * NOTE:
   * Generates a unique internal ID using a Snowflake-like approach. This method
   * is designed
   * to create unique IDs across a distributed system without relying on a central
   * coordinating service (like an auto-incrementing database column).
   * The generated ID is a 64-bit long integer, structured as follows:
   * +-------------------------------------------------------------------------------------------------------------------+
   * | Timestamp (41 bits) | ShardID(10) | Sequence(12) | |
   * +---------------------------------------+------------------------+---------------------+----------------------------+
   * | 63 | 22 | 12 | 0 (Bit positions) |
   * +-------------------------------------------------------------------------------------------------------------------+
   * 1. Timestamp (41 bits):
   * - Represents the milliseconds elapsed since a custom epoch (in this case,
   * 2023-01-01 00:00:00 UTC, defined by the 'epoch' variable). Using 41 bits
   * allows for 2^41 milliseconds, which is approximately 69.7 years.
   * - Calculation: `System.currentTimeMillis() - epoch`
   * - Example: If the current time is 2024-03-01 10:30:15.500 UTC, and the epoch
   * is 2023-01-01 00:00:00.000 UTC, the timestamp would be 39,486,615,500
   * milliseconds.
   * - Lifespan: The 41 bits provide a lifespan of approximately 69.7 years before
   * the timestamp wraps around. Beyond this, you'd need a strategy to avoid
   * collisions (new epoch, re-sharding, etc.).
   * 2^41 milliseconds = 2,199,023,255,552 milliseconds
   * ≈ 69.7 years
   * 2. Shard ID (10 bits):
   * - Identifies the specific database shard or application instance generating
   * the ID. This is crucial for distributing ID generation across multiple
   * servers. 10 bits allow for 2^10 = 1024 unique shard IDs.
   * - Example: A shard ID of 5 would be represented in binary as `0000000101`.
   * 3. Sequence Number (12 bits):
   * - A counter that increments for each ID generated within the *same
   * millisecond*
   * on the *same shard*. 12 bits allow for 2^12 = 4096 unique sequence numbers
   * per millisecond per shard. This handles high-volume scenarios where many
   * IDs might be generated very close together in time.
   * - Example: If this is the 123rd ID generated in the current millisecond on
   * this
   * shard, the sequence number would be 123 (binary `000001111011`).
   * - Atomic Increment: The `sequence.getAndIncrement()` method ensures that the
   * sequence number is incremented atomically, preventing race conditions in a
   * multi-threaded environment.
   * - Bitwise AND (`& 0xFFF`): This operation ensures the sequence number stays
   * within
   * the 12-bit range (0-4095). `0xFFF` is a hexadecimal representation of 4095.
   * The bitwise AND effectively masks all but the last 12 bits.
   * Bitwise Operations:
   * - Left Shift (`<<`):
   * - `timestamp << 22`: Shifts the 41-bit timestamp 22 positions to the left.
   * This
   * creates 22 empty bits (filled with zeros) on the right-hand side of the
   * timestamp. These 22 bits are reserved for the shard ID (10 bits) and the
   * sequence number (12 bits). The left shift is effectively multiplying by 2^22.
   *
   * Example:
   * Timestamp (binary, simplified): `1001...00100`
   * Timestamp << 22: `1001...00100000000000000000000000000`
   *
   * - `shardId << 12`: Shifts the 10-bit shard ID 12 positions to the left,
   * creating
   * 12 empty bits on the right for the sequence number. This is multiplying by
   * 2^12
   *
   * Example:
   * Shard ID (binary): `0000000101`
   * Shard ID << 12: `0000000101000000000000`
   *
   * - Bitwise OR (`|`):
   * - Combines the shifted timestamp, shifted shard ID, and sequence number.
   * Because
   * the left shifts created non-overlapping sections of bits, the bitwise OR
   * effectively concatenates the three values into a single 64-bit integer.
   *
   * Example:
   * ```
   * +---------------------------------------------------------------------------------------------+
   * | (timestamp << 22) | (shardId << 12) | sequenceNumber |
   * +--------------------------------------------+------------------------------+-----------------+
   * | 1001...00100000000000000000000000000 | 0000000101000000000000 |
   * 000001111011 |
   * +---------------------------------------------------------------------------------------------+
   * 
   * Resulting 64-bit ID: 1001...001000000000101000001111011
   * ```
   *
   * Visual Representation:
   *
   * +--------------------------------------------------------------------------------------------+
   * | ID (64 bits) |
   * +--------------------------------------------------------------------------------------------+
   * | Timestamp (41 bits) | Shard ID (10 bits) | Seq (12 bits) |
   * +-------------------------------+------------------------------------+-----------------------+
   * | 10010011...00100 0000000000 | 0000000101 | 000001111011 |
   * +-------------------------------+------------------------------------+-----------------------+
   *
   * @param shardId The ID of the database shard (0-1023).
   * @return The generated 64-bit unique internal ID.
   */
  private synchronized long generateInternalId(int shardId) {
    // Calculate the elapsed milliseconds since the epoch.
    long timestamp = System.currentTimeMillis() - epoch;

    // Atomically increment the sequence counter and get the next value.
    // The bitwise AND operation (& 0xFFF) ensures the sequence number
    // stays within the 12-bit range (0-4095).
    int sequenceNumber = sequence.getAndIncrement() & 0xFFF;

    // Combine the timestamp, shard ID, and sequence number using bitwise shifts
    // and the bitwise OR operator.
    return (timestamp << 22) | (shardId << 12) | sequenceNumber;
  }

  /*
   * NOTE:
   * Further Considerations:
   * NOTE:
   * 1. **Sequence Overflow:** This code *doesn't* handle sequence overflow within
   * a
   * millisecond. In extremely high-volume scenarios (more than 4096 requests per
   * millisecond *per shard*), you'd need additional logic (wait, throw an error,
   * etc., as described in previous responses).
   * 
   * 2. **Clock Drift/Jump:** This code *doesn't* handle system clock changes. In
   * a
   * production system, you *must* address clock drift/jumps (see previous
   * responses
   * for detailed solutions, including `lastTimestamp` tracking and
   * `tilNextMillis()`).
   * 
   * 3. **Shard ID assignment:** This method take shard id as a parameter, but in
   * prod env we have to designe a reliable
   * sharding logic so that evry url will go to the same shard
   */

  /**
   * Calculates the SHA-256 hash of a given string.
   *
   * @param input The string to hash.
   * @return The SHA-256 hash as a byte array.
   * @throws IllegalStateException If the SHA-256 algorithm is not found (should
   *                               never happen).
   */
  private byte[] calculateHash(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
      return digest.digest(input.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Hashing algorithm not found: " + HASH_ALGORITHM, e);
    }
  }

  /**
   * Converts a long integer to a Base62 string.
   *
   * @param id The long integer to convert.
   * @return The Base62 representation of the integer.
   */
  private String toBase62(long id) {
    String characters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    StringBuilder sb = new StringBuilder();
    while (id > 0) {
      sb.insert(0, characters.charAt((int) (id % 62)));
      id /= 62;
    }
    return sb.toString();
  }

  /**
   * Converts a Base62 string to a long integer.
   *
   * @param shortCode The Base62 string to convert.
   * @return The long integer representation of the Base62 string.
   * @throws IllegalArgumentException If the input string contains invalid Base62
   *                                  characters.
   */
  private long fromBase62(String shortCode) {
    String characters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    long id = 0;
    for (char c : shortCode.toCharArray()) {
      id = id * 62 + characters.indexOf(c);
      if (characters.indexOf(c) == -1) {
        throw new IllegalArgumentException("Invalid Base62 character: " + c);
      }
    }
    return id;
  }

  @PreDestroy
  public void shutdownExecutorService() {
    executorService.shutdown(); // Prevents new tasks from being submitted
    try {
      // Wait for existing tasks to finish (with a timeout)
      if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
        executorService.shutdownNow(); // Forcefully shut down if tasks don't finish
      }
    } catch (InterruptedException e) {
      executorService.shutdownNow();
    }
  }
}
