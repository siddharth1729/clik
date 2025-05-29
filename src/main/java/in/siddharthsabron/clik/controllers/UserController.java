package in.siddharthsabron.clik.controllers;

import in.siddharthsabron.clik.dto.UserRegistrationRequest;
import in.siddharthsabron.clik.models.authentications.User;
import in.siddharthsabron.clik.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.List;
import java.util.Optional;
import in.siddharthsabron.clik.repositories.ShortUrlRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody UserRegistrationRequest request) {
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

    @GetMapping("/links")
    public ResponseEntity<?> getUserLinks(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }
        List<in.siddharthsabron.clik.models.links.ShortUrl> links = shortUrlRepository.findAllByUser_UserId(userId);
        return ResponseEntity.ok(links);
    }
} 