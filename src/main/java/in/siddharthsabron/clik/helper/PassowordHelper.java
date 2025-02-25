package in.siddharthsabron.clik.helper;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PassowordHelper {  
    public String setPassword (String password){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    } 
}
