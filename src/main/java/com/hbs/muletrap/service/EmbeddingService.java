package com.hbs.muletrap.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static com.hbs.muletrap.constants.MuleTrapConstants.*;

@Service
public class EmbeddingService {

    private final WebClient webClient = WebClient.create(OLLAMA_URL);

    @SuppressWarnings("unchecked")
    public Mono<float[]> generateEmbedding(String prompt) {
        return webClient.post()
                .uri(OLLAMA_EMBEDDINGS_URL)
                .bodyValue(Map.of(OLLAMA_EMBEDDINGS_REQ_MODEL_KEY, OLLAMA_EMBEDDINGS_REQ_MODEL_VALUE, OLLAMA_EMBEDDINGS_REQ_PROMPT_KEY, prompt))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    // Extract the raw list of numbers
                    List<Number> raw = (List<Number>) response.get(OLLAMA_EMBEDDINGS_RES_EMBEDDING_KEY);
                    // Copy into a float[]
                    float[] vec = new float[raw.size()];
                    for (int i = 0; i < raw.size(); i++) {
                        vec[i] = raw.get(i).floatValue();
                    }
                    return vec;
                });
    }
}