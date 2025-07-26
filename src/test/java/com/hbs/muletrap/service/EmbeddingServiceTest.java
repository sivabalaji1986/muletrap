package com.hbs.muletrap.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class EmbeddingServiceTest {
    @Test
    void testGenerateEmbeddingSuccess() {
        WebClient mockClient = Mockito.mock(WebClient.class, Mockito.RETURNS_DEEP_STUBS);
        EmbeddingService svc = new EmbeddingService(mockClient);

        Mockito.when(mockClient.post().uri("/api/embeddings").bodyValue(Mockito.any())
                        .retrieve().bodyToMono(Map.class))
                .thenReturn(Mono.just(Map.of("embedding", java.util.List.of(0.1f, 0.2f))));

        StepVerifier.create(svc.generateEmbedding("test"))
                .assertNext(vector -> assertArrayEquals(new float[]{0.1f, 0.2f}, vector))
                .verifyComplete();
    }

    @Test
    void testGenerateEmbeddingError() {
        WebClient mockClient = Mockito.mock(WebClient.class, Mockito.RETURNS_DEEP_STUBS);
        EmbeddingService svc = new EmbeddingService(mockClient);

        Mockito.when(mockClient.post().uri("/api/embeddings").bodyValue(Mockito.any())
                        .retrieve().bodyToMono(Map.class))
                .thenReturn(Mono.error(new WebClientResponseException(500, "err", null, null, null)));

        StepVerifier.create(svc.generateEmbedding("test"))
                .expectError(WebClientResponseException.class)
                .verify();
    }
}