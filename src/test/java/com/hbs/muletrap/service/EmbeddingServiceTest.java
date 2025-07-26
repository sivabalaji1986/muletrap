package com.hbs.muletrap.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class EmbeddingServiceTest {

    @Test
    void testGenerateEmbeddingSuccess() {
        // Arrange
        RestTemplate mockRest = Mockito.mock(RestTemplate.class);
        EmbeddingService svc = new EmbeddingService(mockRest);
        // Prepare fake response
        Map<String, Object> fakeResponse = Map.of(
                "embedding", List.of(0.1f, 0.2f)
        );
        // Stub RestTemplate
        Mockito.when(mockRest.postForObject(
                eq("/api/embeddings"),
                any(),
                eq(Map.class)
        )).thenReturn(fakeResponse);

        // Act
        float[] result = svc.generateEmbedding("test prompt");

        // Assert
        assertArrayEquals(new float[]{0.1f, 0.2f}, result);
    }

    @Test
    void testGenerateEmbeddingEmpty() {
        // Arrange
        RestTemplate mockRest = Mockito.mock(RestTemplate.class);
        EmbeddingService svc = new EmbeddingService(mockRest);
        // Empty list response
        Map<String, Object> emptyResponse = Map.of(
                "embedding", List.of()
        );
        Mockito.when(mockRest.postForObject(
                eq("/api/embeddings"), any(), eq(Map.class)
        )).thenReturn(emptyResponse);

        // Act
        float[] result = svc.generateEmbedding("any prompt");

        // Assert: empty array
        assertArrayEquals(new float[]{}, result);
    }
}
