package in.siddharthsabron.clik;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = "in.siddharthsabron.clik")
@EnableJpaAuditing
@PropertySources({@PropertySource("classpath:application.properties"),
                @PropertySource("classpath:/profiles/${spring.profiles.active}.application.properties")})
public class ClikApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClikApplication.class, args);
    }

}
