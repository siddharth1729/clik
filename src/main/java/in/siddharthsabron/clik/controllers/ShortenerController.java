package in.siddharthsabron.clik.controllers;

import in.siddharthsabron.clik.dto.ShortenRequest;
import in.siddharthsabron.clik.services.ShortenerService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ShortenerService shortenerService;

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

        if (userId != null) {
            // User is logged in
            shortCode = shortenerService.shortenUrl(request.getLongUrl(), userId);
            logger.info("=====Shortened URL created: {} for userId: {}", shortCode, userId);
        } else {
            // Anonymous user
            
            shortCode = shortenerService.shortenUrl(request.getLongUrl(), userId);
            logger.info("Shortened URL created: {} for userId: {}", shortCode, userId);
        }

        String shortUrl = "http://localhost:8080/s/" + shortCode;
        return new ResponseEntity<>(shortUrl, HttpStatus.OK);
    }

    
}