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

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody UserRegistrationRequest request) {
        logger.info("Registering user: {}", request.getEmail());
        User newUser = userService.createUser(request.getFirstName(), request.getLastName(), request.getEmail(), request.getPassword());
        logger.info("User registered successfully: {}", newUser);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }
} 