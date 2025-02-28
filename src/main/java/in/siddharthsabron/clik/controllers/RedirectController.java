package in.siddharthsabron.clik.controllers;


import in.siddharthsabron.clik.services.ShortenerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

@RestController
@RequestMapping("/s")
public class RedirectController {

    private static final Logger logger = LoggerFactory.getLogger(RedirectController.class);

    @Autowired
    private ShortenerService shortenerService;

    /**
     * Redirects a short code to its corresponding long URL.
     *
     * @param shortCode The short code to resolve.
     * @return A RedirectView to the long URL, or a redirect to a 404 page if not found.
     */
    @GetMapping("/{shortCode}")
    public RedirectView redirect(@PathVariable String shortCode) {
        logger.info("====Redirecting short code: {} to long URL", shortCode);
        Optional<String> longUrl = shortenerService.getLongUrl(shortCode);
        if (longUrl.isPresent()) {
            return new RedirectView(longUrl.get());
        } else {
            logger.warn("Short code not found: {}", shortCode);
            return new RedirectView("/404");
        }
    }
    
}
