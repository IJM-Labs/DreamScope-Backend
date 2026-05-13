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

    public String titleFor(String dreamContent, String interpretationText) {
        if (apiKey == null || apiKey.isBlank()) {
            return fallbackTitle(dreamContent);
        }

        try {
            Map<?, ?> response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(Map.of(
                            "model", model,
                            "messages", List.of(
                                    Map.of("role", "system", "content", "Create a calm, short chat title for a dream journal. Return only the title, maximum 6 words."),
                                    Map.of("role", "user", "content", "Dream: " + dreamContent + "\nInterpretation: " + interpretationText)
                            ),
                            "temperature", 0.4
                    ))
                    .retrieve()
                    .body(Map.class);

            List<?> choices = (List<?>) response.get("choices");
            Map<?, ?> firstChoice = (Map<?, ?>) choices.getFirst();
            Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
            return cleanTitle((String) message.get("content"), dreamContent);
        } catch (RuntimeException exception) {
            LOGGER.warn("AI title generation failed. Returning fallback title.", exception);
            return fallbackTitle(dreamContent);
        }
    }

    private String fallbackInterpretation() {
        return "Demo interpretation: This dream may point to feelings, concerns, or wishes the user is processing. Add OPENAI_API_KEY for a real AI interpretation.";
    }

    private String fallbackTitle(String dreamContent) {
        if (dreamContent == null || dreamContent.isBlank()) {
            return "New dream chat";
        }
        String cleaned = dreamContent.trim().replaceAll("\\s+", " ");
        int sentenceEnd = firstSentenceEnd(cleaned);
        if (sentenceEnd > 0) {
            cleaned = cleaned.substring(0, sentenceEnd);
        }
        return cleaned.length() > 42 ? cleaned.substring(0, 42).trim() : cleaned;
    }

    private int firstSentenceEnd(String text) {
        int end = -1;
        for (char marker : new char[]{'.', '!', '?'}) {
            int index = text.indexOf(marker);
            if (index >= 0 && (end == -1 || index < end)) {
                end = index;
            }
        }
        return end;
    }

    private String cleanTitle(String title, String dreamContent) {
        if (title == null || title.isBlank()) {
            return fallbackTitle(dreamContent);
        }
        String cleaned = title.replace("\"", "").replace("Title:", "").trim();
        return cleaned.length() > 48 ? cleaned.substring(0, 48).trim() : cleaned;
    }
}
