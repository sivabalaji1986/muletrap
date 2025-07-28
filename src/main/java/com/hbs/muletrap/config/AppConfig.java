package com.hbs.muletrap.config;

import com.hbs.muletrap.constants.MuleTrapConstants;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    @Bean
    public RestTemplate ollamaRestTemplate(RestTemplateBuilder builder) {
        return builder
                .rootUri(MuleTrapConstants.OLLAMA_URL)
                .build();
    }
}
