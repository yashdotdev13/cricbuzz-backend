package com.company.cricbuzz_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cricbuzzOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cricbuzz Backend API")
                        .version("1.0.0")
                        .description("""
                                REST APIs for the Cricbuzz-like real-time cricket
                                score and commentary backend.
                                """)
                        .contact(new Contact()
                                .name("Yash Chauhan")
                                .url("https://github.com/yashdotdev13"))
                        .license(new License()
                                .name("MIT License")));
    }
}