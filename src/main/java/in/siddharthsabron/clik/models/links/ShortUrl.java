package in.siddharthsabron.clik.models.links;

import java.security.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.siddharthsabron.clik.models.authentications.User;
import in.siddharthsabron.clik.models.baseModels.AuditMetadata;
import jakarta.persistence.*;

/**
 * Entity representing a shortened URL.
 */
@Entity
@Table(name = "short_urls")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShortUrl extends AuditMetadata {

    @Id
    @Column(name = "internal_id", nullable = false)
    private Long internalId;

    @Column(name = "short_code", length = 10, nullable = false)
    private String shortCode;

    @Column(name = "long_url", columnDefinition = "TEXT", nullable = false)
    private String longUrl;

    @Column(name = "long_url_hash", columnDefinition = "BINARY(32)", nullable = false)
    private byte[] longUrlHash;

    @Column(name = "expires_at")
    private Timestamp expiresAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "click_count", columnDefinition = "BIGINT UNSIGNED DEFAULT 0")
    private Long clickCount = 0L;

    @Column(name = "shard_id", nullable = false)
    private Integer shardId;


    public ShortUrl() {}

    public ShortUrl(Long internalId, String shortCode, String longUrl, byte[] longUrlHash, Integer shardId) {
        this.internalId = internalId;
        this.shortCode = shortCode;
        this.longUrl = longUrl;
        this.longUrlHash = longUrlHash;
        this.shardId = shardId;
        this.clickCount = 0L;
    }

    // Getters and Setters
    public Long getInternalId() { return internalId; }
    public void setInternalId(Long internalId) { this.internalId = internalId; }
    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public String getLongUrl() { return longUrl; }
    public void setLongUrl(String longUrl) { this.longUrl = longUrl; }
    public byte[] getLongUrlHash() { return longUrlHash; }
    public void setLongUrlHash(byte[] longUrlHash) { this.longUrlHash = longUrlHash; }
    public Timestamp getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Timestamp expiresAt) { this.expiresAt = expiresAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Long getClickCount() { return clickCount; }
    public void setClickCount(Long clickCount) { this.clickCount = clickCount; }
    public Integer getShardId() { return shardId; }
    public void setShardId(Integer shardId) { this.shardId = shardId; }
}