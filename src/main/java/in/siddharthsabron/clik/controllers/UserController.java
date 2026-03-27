package in.siddharthsabron.clik.controllers;

import in.siddharthsabron.clik.dto.ErrorResponseDto;
import in.siddharthsabron.clik.dto.ShortUrlResponseDto;
import in.siddharthsabron.clik.dto.UserRegistrationRequest;
import in.siddharthsabron.clik.models.authentications.User;
import in.siddharthsabron.clik.models.links.ShortUrl;
import in.siddharthsabron.clik.repositories.UserRepository;
import in.siddharthsabron.clik.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    // ─── Registration ────────────────────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody UserRegistrationRequest request) throws Exception {
        logger.info("Registering user: {}", request.getEmail());
        User newUser = userService.createUser(
                request.getFirstName(), request.getLastName(),
                request.getEmail(), request.getPassword());
        logger.info("User registered successfully: {}", newUser.getEmail());
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    // ─── Login ───────────────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserRegistrationRequest request, HttpSession session) {
        Optional<User> userOpt = userService.findByEmail(request.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (encoder.matches(request.getPassword(), user.getPassword())) {
                session.setAttribute("userId", user.getUserId());
                logger.info("User logged in: {}", user.getEmail());
                return ResponseEntity.ok(safeUserPayload(user));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDto("Invalid credentials", HttpStatus.UNAUTHORIZED.value()));
    }

    // ─── Logout ──────────────────────────────────────────────────────────────────

    /**
     * POST /api/users/logout
     * Invalidates the server-side session. Returns 200 regardless of whether a
     * session was active, so the client can always treat it as success.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        logger.info("Session invalidated");
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // ─── Session check ───────────────────────────────────────────────────────────

    /**
     * GET /api/users/me
     * Returns the authenticated user's safe profile (no password hash).
     * Called by the frontend on page load to restore UI state from an active session.
     * Returns 401 if no valid session exists.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getSessionUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseDto("Not authenticated", HttpStatus.UNAUTHORIZED.value()));
        }
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isEmpty()) {
            session.invalidate();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseDto("Session user not found", HttpStatus.UNAUTHORIZED.value()));
        }
        return ResponseEntity.ok(safeUserPayload(userOpt.get()));
    }

    // ─── Session-scoped link history ─────────────────────────────────────────────

    /**
     * GET /api/users/links
     * Returns the authenticated user's shortened links based on the active session.
     * Called by the frontend {@code checkSession()} and {@code fetchUserLinks()}.
     * Returns 401 if no valid session exists, 200 with an empty array if the user
     * has no links yet.
     */
    @GetMapping("/links")
    public ResponseEntity<?> getSessionLinks(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseDto("Not authenticated", HttpStatus.UNAUTHORIZED.value()));
        }
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isEmpty()) {
            session.invalidate();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseDto("Session user not found", HttpStatus.UNAUTHORIZED.value()));
        }

        List<ShortUrl> shortUrls = userRepository.findAllShortUrlsByUserEmail(userOpt.get().getEmail());
        logger.info("Fetched {} links for session user: {}", shortUrls.size(), userOpt.get().getEmail());

        List<ShortUrlResponseDto> dtos = shortUrls.stream()
                .map(url -> new ShortUrlResponseDto(
                        url.getInternalId(),
                        url.getShortCode(),
                        url.getLongUrl(),
                        url.getCreatedAt(),
                        url.getClickCount(),
                        url.getUser() != null ? url.getUser().getEmail() : null))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // ─── Email-scoped link history (kept for API compatibility) ──────────────────

    /**
     * GET /api/users/links/{email}
     * Original endpoint — still available for direct API usage.
     */
    @GetMapping("/links/{email}")
    public ResponseEntity<?> getAllShortUrlsByUserEmail(@PathVariable String email) {
        try {
            logger.info("Fetching short URLs for email: {}", email);
            List<ShortUrl> shortUrls = userRepository.findAllShortUrlsByUserEmail(email);

            if (shortUrls.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponseDto(
                                "No short URLs found for user with email: " + email,
                                HttpStatus.NOT_FOUND.value()));
            }

            List<ShortUrlResponseDto> dtos = shortUrls.stream()
                    .map(url -> new ShortUrlResponseDto(
                            url.getInternalId(),
                            url.getShortCode(),
                            url.getLongUrl(),
                            url.getCreatedAt(),
                            url.getClickCount(),
                            url.getUser() != null ? url.getUser().getEmail() : null))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("Error fetching short URLs for email: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto(
                            "Error fetching short URLs: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    /**
     * Returns a safe Map representation of a User — no password hash exposed.
     */
    private Map<String, Object> safeUserPayload(User user) {
        return Map.of(
                "userId",    user.getUserId(),
                "email",     user.getEmail(),
                "firstName", user.getFirstName() != null ? user.getFirstName() : "",
                "lastName",  user.getLastName()  != null ? user.getLastName()  : ""
        );
    }
}
