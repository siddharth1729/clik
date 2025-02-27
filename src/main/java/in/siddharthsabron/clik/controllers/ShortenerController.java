package in.siddharthsabron.clik.controllers;

import in.siddharthsabron.clik.data.ShortenRequest;
import in.siddharthsabron.clik.services.ShortenerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

/**
 * Controller for handling URL shortening and redirection requests.
 */
@RestController
@RequestMapping("/api")
public class ShortenerController {

    @Autowired
    private ShortenerService shortenerService;

    /**
     * Shortens a long URL.
     * Requires a logged-in user (userId obtained from the session).
     *
     * @param request The request body containing the long URL.
     * @param session The HTTP session.
     * @return A ResponseEntity containing the short URL or an error message.
     */
    @PostMapping("/shorten")
    public ResponseEntity<String> shortenUrl(@RequestBody ShortenRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return new ResponseEntity<>("User not logged in", HttpStatus.UNAUTHORIZED);
        }

        String shortCode = shortenerService.shortenUrl(request.getLongUrl(), userId);
        String shortUrl = "http://localhost:8080/s/" + shortCode; // Replace with your domain
        return new ResponseEntity<>(shortUrl, HttpStatus.OK);
    }

    /**
     * Redirects a short code to its corresponding long URL.
     *
     * @param shortCode The short code to resolve.
     * @return A RedirectView to the long URL, or a redirect to a 404 page if not found.
     */
    @GetMapping("/s/{shortCode}")
    public RedirectView redirect(@PathVariable String shortCode) {
        Optional<String> longUrl = shortenerService.getLongUrl(shortCode);
        if (longUrl.isPresent()) {
            return new RedirectView(longUrl.get());
        } else {
            return new RedirectView("/404"); // Redirect to a 404 page (you'll need to create this)
        }
    }
}