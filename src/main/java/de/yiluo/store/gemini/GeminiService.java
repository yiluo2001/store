package de.yiluo.store.gemini;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiService(
            @Value("${gemini.proxy-base-url}") String proxyBaseUrl,
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model) {
        
        this.apiKey = apiKey;
        this.model = model;
        
        this.restClient = RestClient.builder()
                .baseUrl(proxyBaseUrl)
                .build();
    }

    public String chat(String userMessage) {
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", userMessage)
                ))
            )
        );

        try {
            Map response = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent?key={apiKey}", model, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            return extractTextFromResponse(response);

        } catch (Exception e) {
            return "Error communicating with Gemini proxy: " + e.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            return "Failed to parse AI response structure.";
        }
    }
}