package com.hbs.muletrap.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.hbs.muletrap.constants.MuleTrapConstants.*;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI muleTrapOpenAPI() {
        return new OpenAPI()
                .info(new Info().title(OPEN_API_SPEC_TITLE)
                        .description(OPEN_API_SPEC_DESCRIPTION)
                        .version(OPEN_API_SPEC_VERSION));
    }
}