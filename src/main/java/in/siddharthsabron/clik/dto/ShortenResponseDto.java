package in.siddharthsabron.clik.dto;

import java.time.Instant;

/**
 * Response payload returned by POST /api/shorten.
 * Carries the full short URL plus live stats so the UI can
 * display them immediately without a second round-trip.
 */
public class ShortenResponseDto {

    private String shortUrl;
    private String shortCode;
    private String longUrl;
    private Long clickCount;
    private Instant createdAt;

    public ShortenResponseDto(String shortUrl, String shortCode, String longUrl,
                              Long clickCount, Instant createdAt) {
        this.shortUrl = shortUrl;
        this.shortCode = shortCode;
        this.longUrl = longUrl;
        this.clickCount = clickCount != null ? clickCount : 0L;
        this.createdAt = createdAt;
    }

    public String getShortUrl()   { return shortUrl; }
    public String getShortCode()  { return shortCode; }
    public String getLongUrl()    { return longUrl; }
    public Long   getClickCount() { return clickCount; }
    public Instant getCreatedAt() { return createdAt; }
}
