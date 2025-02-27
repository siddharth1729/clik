package in.siddharthsabron.clik.models.links;

import jakarta.persistence.*;
import java.sql.Timestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import in.siddharthsabron.clik.models.authentications.User;
import in.siddharthsabron.clik.models.baseModels.AuditMetadata;

@Entity
@Table(name = "short_urls")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Link extends AuditMetadata{

    @Id
    @Column(name = "internal_id", nullable = false)
    private Long internalId;  // New: Internal ID

    @Column(name = "short_code", length = 10, nullable = false)
    private String shortCode; //Still here but not id

    @Column(name = "long_url", columnDefinition = "TEXT", nullable = false)
    private String longUrl;

    @Column(name = "expires_at")
    private Timestamp expiresAt;

    @ManyToOne
    @JoinColumn(name = "user_id") 
    private User user;

    @Column(name = "click_count", columnDefinition = "BIGINT UNSIGNED DEFAULT 0")
    private Long clickCount;

    @Column(name = "shard_id", nullable = false)
    private Integer shardId;

    public Link() {}

    public Link(Long internalId, String shortCode, String longUrl, Integer shardId) {
        this.internalId = internalId;
        this.shortCode = shortCode;
        this.longUrl = longUrl;
        this.shardId = shardId;
    }

    public Long getInternalId() { return internalId; }
    public void setInternalId(Long internalId) { this.internalId = internalId; }
    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public String getLongUrl() { return longUrl; }
    public void setLongUrl(String longUrl) { this.longUrl = longUrl; }
    public Timestamp getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Timestamp expiresAt) { this.expiresAt = expiresAt; }
    public User getUser() { return user; }  
    public void setUser(User user) { this.user = user; }
    public Long getClickCount() { return clickCount; }
    public void setClickCount(Long clickCount) { this.clickCount = clickCount; }
    public Integer getShardId() { return shardId; }
    public void setShardId(Integer shardId) { this.shardId = shardId; }
}