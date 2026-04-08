package in.siddharthsabron.clik.controllers;

import in.siddharthsabron.clik.dto.ShortenRequest;
import in.siddharthsabron.clik.dto.ShortenResponseDto;
import in.siddharthsabron.clik.models.links.ShortUrl;
import in.siddharthsabron.clik.services.ShortenerService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles URL shortening requests.
 */
@RestController
@RequestMapping("/api")
public class ShortenerController {

    private static final Logger logger = LoggerFactory.getLogger(ShortenerController.class);

    /**
     * Injected from application.properties: app.base-url
     * Falls back to http://localhost:8080 when the property is absent (local dev).
     */
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private final ShortenerService shortenerService;

    public ShortenerController(ShortenerService shortenerService) {
        this.shortenerService = shortenerService;
    }

    /**
     * Shortens a long URL.
     *
     * <p>User resolution order:
     * <ol>
     *   <li>HttpSession — set automatically on login; works for browser clients.</li>
     *   <li>Request body {@code userId} — backwards-compatible for API clients.</li>
     *   <li>Neither present → anonymous link (user_id = NULL in DB).</li>
     * </ol>
     *
     * @param shortenRequest Body containing {@code longUrl} and an optional {@code userId}.
     * @param session        The current HTTP session (may be new/empty).
     * @return {@link ShortenResponseDto} with the full short URL and live stats.
     */
    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponseDto> shortenUrl(
            @RequestBody @Valid ShortenRequest shortenRequest,
            HttpSession session) {

        // Prefer the authenticated session; fall back to an explicit userId in the body.
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            userId = shortenRequest.getUserId();
        }
        
        logger.info("Shorten request — longUrl: {}, resolved userId: {}", shortenRequest.getLongUrl(), userId);

        ShortUrl shortUrl = shortenerService.shortenUrl(shortenRequest.getLongUrl(), userId);

        String fullShortUrl = baseUrl + "/s/" + shortUrl.getShortCode();
        logger.info("Short URL created: {}", fullShortUrl);

        ShortenResponseDto response = new ShortenResponseDto(
                fullShortUrl,
                shortUrl.getShortCode(),
                shortUrl.getLongUrl(),
                shortUrl.getClickCount(),
                shortUrl.getCreatedAt()
        );

        return ResponseEntity.ok(response);
    }
}
