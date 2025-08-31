package in.siddharthsabron.clik.controllers;

import in.siddharthsabron.clik.dto.UserRegistrationRequest;
import in.siddharthsabron.clik.dto.ShortUrlResponseDto;
import in.siddharthsabron.clik.dto.ErrorResponseDto;
import in.siddharthsabron.clik.models.authentications.User;
import in.siddharthsabron.clik.models.links.ShortUrl;
import in.siddharthsabron.clik.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import in.siddharthsabron.clik.repositories.UserRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    /*
     *
     * NOTE: Am using constructor injection because this looks hot and sexy as fuckkkk.
     * 
     */
    private UserService userService;
    private UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody UserRegistrationRequest request) throws Exception {
        logger.info("Registering user: {}", request.getEmail());
        User newUser = userService.createUser(request.getFirstName(), request.getLastName(), request.getEmail(), request.getPassword());
        logger.info("User registered successfully: {}", newUser);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserRegistrationRequest request, HttpSession session) {
        Optional<User> userOpt = userService.findByEmail(request.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (encoder.matches(request.getPassword(), user.getPassword())) {
                session.setAttribute("userId", user.getUserId());
                return ResponseEntity.ok(user);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }

    @GetMapping("/links/{email}")
    public ResponseEntity<?> getAllShortUrlsByUserEmail(@PathVariable String email) throws Exception {
        try {
            logger.info("Fetching short URLs for email: {}", email);
            List<ShortUrl> shortUrls = userRepository.findAllShortUrlsByUserEmail(email);
            logger.info("Number of short URLs found: {}", shortUrls.size());
            logger.info("Short URLs details: {}", shortUrls.stream()
                .map(url -> "ID: %d, Code: %s, URL: %s".formatted(
                        url.getInternalId(),
                        url.getShortCode(),
                        url.getLongUrl()))
                .collect(Collectors.joining(", ")));
            
            if (shortUrls.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto("No short URLs found for user with email: " 
                    + email, HttpStatus.NOT_FOUND.value()));
            }
            
            List<ShortUrlResponseDto> responseDtos = shortUrls.stream()
                .map(url -> new ShortUrlResponseDto(
                    url.getInternalId(),
                    url.getShortCode(),
                    url.getLongUrl(),
                    url.getCreatedAt(),
                    url.getClickCount(),
                    url.getUser() != null ? url.getUser().getEmail() : null
                ))
                .collect(Collectors.toList());
            
            logger.info("Converted to DTOs: {}", responseDtos.size());
            return ResponseEntity.ok(responseDtos);
        } catch (Exception e) {
            logger.error("Error fetching short URLs for email: " + email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto("Error fetching short URLs: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
} 