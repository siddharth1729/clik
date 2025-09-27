package in.siddharthsabron.clik.dto;

import lombok.Data;

/**
 * Data transfer object (DTO) for the URL shortening request body.
 */
@Data
public class ShortenRequest {
    private String longUrl;
    private Long userId;

}