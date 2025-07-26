package com.hbs.muletrap.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    private static final String EMBED_ENDPOINT = "/api/embeddings";
    private final RestTemplate restTemplate;

    public EmbeddingService(RestTemplate ollamaRestTemplate) {
        this.restTemplate = ollamaRestTemplate;
    }

    @SuppressWarnings("unchecked")
    public float[] generateEmbedding(String prompt) {
        Map<String, Object> response = restTemplate.postForObject(
                EMBED_ENDPOINT,
                Map.of("model", "nomic-embed-text", "prompt", prompt),
                Map.class
        );
        List<Number> raw = (List<Number>) response.get("embedding");
        float[] vec = new float[raw.size()];
        for (int i = 0; i < raw.size(); i++) {
            vec[i] = raw.get(i).floatValue();
        }
        return vec;
    }
}