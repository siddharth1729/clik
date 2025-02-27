package in.siddharthsabron.clik.dto;

/**
 * Data transfer object (DTO) for the URL shortening request body.
 */
public class ShortenRequest {
    private String longUrl;

    public ShortenRequest() {}

    public ShortenRequest(String longUrl) {
        this.longUrl = longUrl;
    }

    public String getLongUrl() {
        return this.longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }
}