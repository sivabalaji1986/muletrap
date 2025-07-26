package com.hbs.muletrap.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI muleTrapOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("MuleTrap API")
                        .description("Spring Boot API for Mule Account Detection using Vector Embeddings")
                        .version("1.0"));
    }
}