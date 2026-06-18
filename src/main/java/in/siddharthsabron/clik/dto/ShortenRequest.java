package in.siddharthsabron.clik.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.URL;

/**
 * Data transfer object (DTO) for the URL shortening request body.
 */
@Data
public class ShortenRequest {

  @NotBlank(message = "URL cannot be blank")
  @URL(message = "Please provide a valid URL format")
  private String longUrl;
  private Long userId;
}
