package in.siddharthsabron.clik.dto;

/**
 * Data transfer object (DTO) for the URL shortening request body.
 */
public class ShortenRequest {
    private String longUrl;

    private Long userId;

    public ShortenRequest() {}

    public ShortenRequest(String longUrl , Long userId) {
        this.longUrl = longUrl;
        this.userId = userId;
    }

    public String getLongUrl() {
        return this.longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

}