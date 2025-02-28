package in.siddharthsabron.clik.services;

import in.siddharthsabron.clik.models.links.ShortUrl;
import in.siddharthsabron.clik.repositories.ShortUrlRepository;
//import in.siddharthsabron.clik.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import in.siddharthsabron.clik.models.authentications.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;



/**
 * ShortenerService: Provides the core business logic for shortening and resolving URLs.
 *
 * Key Features:
 * - Handles URL shortening, including duplicate detection and concurrency control.
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

    //@Autowired
    //private UserRepository userRepository;

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
        User user = null;
        // if (userId != null) {
        //     user = userRepository.findById(userId)
        //             .orElseThrow(() -> new IllegalArgumentException("User not found"));
        // }
        return shortenUrlInternal(longUrl, user);
    }

    /**
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
     * Internal method to handle URL shortening, shared by both user and anonymous cases.
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
                logger.info("Incrementing click count for short URL: {}", shortUrl);
                incrementClickCountAsync(shortUrl.getShardId(), internalId);
                logger.info("Returning long URL: {}", shortUrl.getLongUrl());
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
        // Submit a task to the thread pool
       Future<?> future = executorService.submit(() -> { // Note the Future<?>
            try {
                shortUrlRepository.incrementClickCount(shardId, internalId);
            } catch (Exception e) {
                logger.error("Error incrementing click count", e);
            }
        });

        // You can optionally check the status of the task later:
         try {
             future.get(); // This will block until the task completes (or throws an exception)
         } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
             logger.error("Error waiting for click count update", e);
         }
    }


    /**
     * Generates a unique internal ID using a Snowflake-like approach.
     *
     * @param shardId The shard ID.
     * @return The generated internal ID.
     */
    private long generateInternalId(int shardId) {
        long timestamp = System.currentTimeMillis() - epoch;
        int sequenceNumber = sequence.getAndIncrement() & 0xFFF; // Sequence within the millisecond
        return (timestamp << 22) | (shardId << 12) | sequenceNumber;
    }

    /**
     * Calculates the SHA-256 hash of a given string.
     *
     * @param input The string to hash.
     * @return The SHA-256 hash as a byte array.
     * @throws IllegalStateException If the SHA-256 algorithm is not found (should never happen).
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
     * @throws IllegalArgumentException If the input string contains invalid Base62 characters.
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