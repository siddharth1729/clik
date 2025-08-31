package in.siddharthsabron.clik.dto;

import java.time.Instant;

public class ShortUrlResponseDto {
    private Long internalId;
    private String shortCode;
    private String longUrl;
    private Instant createdAt;
    private Long clickCount;
    private String userEmail; // Can be null for anonymous links

    // Constructor
    public ShortUrlResponseDto(Long internalId, String shortCode, String longUrl, Instant createdAt, Long clickCount, String userEmail) {
        this.internalId = internalId;
        this.shortCode = shortCode;
        this.longUrl = longUrl;
        this.createdAt = createdAt;
        this.clickCount = clickCount;
        this.userEmail = userEmail;
    }

    // Getters and Setters
    public Long getInternalId() {
        return internalId;
    }

    public void setInternalId(Long internalId) {
        this.internalId = internalId;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Long getClickCount() {
        return clickCount;
    }

    public void setClickCount(Long clickCount) {
        this.clickCount = clickCount;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
} 