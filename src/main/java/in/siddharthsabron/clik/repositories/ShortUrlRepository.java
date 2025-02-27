// ShortUrlRepository.java (Corrected)
package in.siddharthsabron.clik.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import in.siddharthsabron.clik.models.links.ShortUrl; // Correct import

import java.util.Optional;

/**
 * Repository for managing ShortUrl entities.
 */
@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByShardIdAndInternalId(Integer shardId, Long internalId);
    Optional<ShortUrl> findByShardIdAndShortCode(Integer shardId, String shortCode);
    Optional<ShortUrl> findByInternalId(Long internalId);

    // Pessimistic Locking
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM ShortUrl l WHERE l.shardId = :shardId AND l.longUrlHash = :longUrlHash") // Use *entity* name (ShortUrl), not table name (short_urls)
    Optional<ShortUrl> findByShardIdAndLongUrlHashForUpdate(Integer shardId, byte[] longUrlHash);

    @Modifying
    @Transactional
    @Query(value = "UPDATE short_urls SET click_count = click_count + 1 WHERE shard_id = :shardId AND internal_id = :internalId", nativeQuery = true)
    void incrementClickCount(Integer shardId, Long internalId);
}