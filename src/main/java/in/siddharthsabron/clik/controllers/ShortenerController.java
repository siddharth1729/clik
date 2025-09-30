package in.siddharthsabron.clik.controllers;

import in.siddharthsabron.clik.dto.ShortenRequest;
import in.siddharthsabron.clik.services.ShortenerService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling URL shortening and redirection requests.
 */
@RestController
@RequestMapping("/api")
public class ShortenerController {

    private static final Logger logger = LoggerFactory.getLogger(ShortenerController.class);

 
    private ShortenerService shortenerService;

    public ShortenerController(ShortenerService shortenerService) {
        this.shortenerService = shortenerService;
    }

    /**
     * Shortens a long URL.  Can be used with or without a logged-in user.
     *
     * @param request The request body containing the long URL.
     * @param session The HTTP session (used to get the userId, if present).
     * @return A ResponseEntity containing the short URL.
     */
    @PostMapping("/shorten")
    public ResponseEntity<String> shortenUrl(@RequestBody ShortenRequest request, HttpSession session) {
        logger.info("Received shorten URL request");
        Long userId = (Long) session.getAttribute("userId");
        String shortCode;
        logger.info("=======UserId from session: {}=====", userId);
        
        if (userId != null) {
            // User is logged in - use session userId
            shortCode = shortenerService.shortenUrl(request.getLongUrl(), userId);
            logger.info("=====Shortened URL created: {} for authenticated userId: {}", shortCode, userId);
        } else {
            // Anonymous user - pass null
            shortCode = shortenerService.shortenUrl(request.getLongUrl());
            logger.info("=====Shortened URL created: {} for anonymous user", shortCode);
        }

        String shortUrl = "http://localhost:8080/a/" + shortCode;
        return new ResponseEntity<>(shortUrl, HttpStatus.OK);
    }

    
}