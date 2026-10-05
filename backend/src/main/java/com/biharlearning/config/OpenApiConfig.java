package com.biharlearning.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI biharLearningOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Bihar Class IX Student Learning Platform API")
                .version("v1")
                .description("API-first backend. Content provenance is first-class; "
                        + "no fabricated official data. See docs/api.md.")
                .license(new License().name("Project code: MIT; content belongs to its publishers")));
    }
}
