package com.hbs.muletrap.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static com.hbs.muletrap.constants.MuleTrapConstants.*;

@Service
public class EmbeddingService {

    private final RestTemplate restTemplate;

    public EmbeddingService(RestTemplate ollamaRestTemplate) {
        this.restTemplate = ollamaRestTemplate;
    }

    @SuppressWarnings("unchecked")
    public float[] generateEmbedding(String prompt) {
        Map<String, Object> response = restTemplate.postForObject(
                OLLAMA_EMBEDDINGS_URL,
                Map.of(OLLAMA_EMBEDDINGS_REQ_MODEL_KEY, OLLAMA_EMBEDDINGS_REQ_MODEL_VALUE, OLLAMA_EMBEDDINGS_REQ_PROMPT_KEY, prompt),
                Map.class
        );
        List<Number> raw = (List<Number>) response.get(OLLAMA_EMBEDDINGS_RES_EMBEDDING_KEY);
        float[] vec = new float[raw.size()];
        for (int i = 0; i < raw.size(); i++) {
            vec[i] = raw.get(i).floatValue();
        }
        return vec;
    }
}