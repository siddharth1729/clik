package in.siddharthsabron.clik.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BASIC_AUTH = "basicAuth";

    @Bean
    public OpenAPI clikOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Clik URL Shortener API")
                        .description("API documentation for the Clik URL shortener service")
                        .version("v1")
                        .contact(new Contact()
                                .name("siddharth sabron")
                                .url("https://siddharthsabron.in")
                                .email("siddharth.sabron@gmail.com")))
                .addSecurityItem(new SecurityRequirement().addList(BASIC_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BASIC_AUTH, new SecurityScheme()
                                .name(BASIC_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic"))
                );
    }
}
