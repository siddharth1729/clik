package in.siddharthsabron.clik.config; // Put this in a config package

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((authorize) -> authorize
                    .requestMatchers(new AntPathRequestMatcher("/api/users/register")).permitAll() // Allow anonymous access to user registration
                    .requestMatchers(new AntPathRequestMatcher("/api/shorten")).permitAll() // Allow anonymous access to /api/shorten
                    .requestMatchers(new AntPathRequestMatcher("/s/**")).permitAll()       // Allow access to short URLs
                    .anyRequest().authenticated() // Require authentication for all other requests
            )
            .csrf((csrf) -> csrf.disable()); // Disable CSRF for this example

        return http.build();
    }
}