package in.siddharthsabron.clik.services;

import in.siddharthsabron.clik.models.authentications.User;
import in.siddharthsabron.clik.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    
    private UserRepository userRepository;
    
    public UserService(UserRepository userRepository){
      this.userRepository = userRepository;
  }

    public User createUser(String firstName, String lastName, String email, String password) {
        User user = new User(firstName, lastName, email, password);
        User savedUser = userRepository.save(user);
        logger.info("User created: {}", savedUser);
        return savedUser;
    }
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
} 
