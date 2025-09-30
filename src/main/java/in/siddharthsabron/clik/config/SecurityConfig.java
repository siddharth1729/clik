package in.siddharthsabron.clik.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .httpBasic(basic -> {})
            .authorizeHttpRequests(auth -> auth
                // Allow static resources and root
                .requestMatchers("/", "/index.html", "/static/**").permitAll()
                // Allow application endpoints (current behavior)
                .requestMatchers("/api/**", "/s/**").permitAll()
                // Secure Swagger and OpenAPI endpoints with Basic Auth
                .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**"
                ).authenticated()
                .anyRequest().permitAll()
            );
        
        return http.build();
    }

    @Bean
    public UserDetailsService users() {
        // Hardcoded credentials for Swagger access
        UserDetails swaggerUser = User
                .withUsername("legion")
                .password("{noop}@Learning1729")
                .roles("SWAGGER")
                .build();
        return new InMemoryUserDetailsManager(swaggerUser);
    }
}