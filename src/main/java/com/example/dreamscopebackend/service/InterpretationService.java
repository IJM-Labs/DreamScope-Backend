package com.example.dreamscopebackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class InterpretationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(InterpretationService.class);

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public InterpretationService(
            RestClient.Builder restClientBuilder,
            @Value("${openai.api-key:}") String apiKey,
            @Value("${openai.model:gpt-4.1-mini}") String model
    ) {
        this.restClient = restClientBuilder.baseUrl("https://api.openai.com/v1").build();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model == null ? "gpt-4.1-mini" : model.trim();
    }

    public String interpret(String dreamContent) {
        if (apiKey == null || apiKey.isBlank()) {
            return fallbackInterpretation();
        }

        try {
            Map<?, ?> response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(Map.of(
                            "model", model,
                            "messages", List.of(
                                    Map.of("role", "system", "content", "Du fortolker nattedrømme empatisk og forsigtigt på dansk. Du må ikke påstå kliniske diagnoser."),
                                    Map.of("role", "user", "content", dreamContent)
                            ),
                            "temperature", 0.7
                    ))
                    .retrieve()
                    .body(Map.class);

            List<?> choices = (List<?>) response.get("choices");
            Map<?, ?> firstChoice = (Map<?, ?>) choices.getFirst();
            Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
            return (String) message.get("content");
        } catch (RuntimeException exception) {
            LOGGER.warn("AI interpretation failed. Returning fallback interpretation.", exception);
            return fallbackInterpretation();
        }
    }

    private String fallbackInterpretation() {
        return "Demo-fortolkning: Drømmen kan pege på følelser, bekymringer eller ønsker, som brugeren arbejder med. Tilføj OPENAI_API_KEY for en rigtig AI-fortolkning.";
    }
}
