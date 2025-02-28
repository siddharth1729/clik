package in.siddharthsabron.clik.config;

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
                    .requestMatchers(new AntPathRequestMatcher("/api/users/register")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/api/shorten")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/s/**")).permitAll()
                    .anyRequest().authenticated()
            )
            .csrf((csrf) -> csrf.disable());

        return http.build();
    }
}