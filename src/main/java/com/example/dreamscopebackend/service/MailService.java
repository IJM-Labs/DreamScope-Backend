package com.example.dreamscopebackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class MailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

    private final RestClient restClient;
    private final String resendApiKey;
    private final String fromEmail;
    private final String frontendUrl;

    public MailService(
            RestClient.Builder restClientBuilder,
            @Value("${resend.api-key:}") String resendApiKey,
            @Value("${resend.from:no-reply@dreamscope.local}") String fromEmail,
            @Value("${dreamscope.frontend-url:http://localhost:3000}") String frontendUrl
    ) {
        this.restClient = restClientBuilder.baseUrl("https://api.resend.com").build();
        this.resendApiKey = resendApiKey == null ? "" : resendApiKey.trim();
        this.fromEmail = fromEmail == null ? "" : fromEmail.trim();
        this.frontendUrl = frontendUrl;
    }

    public void sendOneTimeCode(String email, String code) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            LOGGER.info("DreamScope engangskode for {}: {}", email, code);
            return;
        }

        try {
            restClient.post()
                    .uri("/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + resendApiKey)
                    .body(Map.of(
                            "from", fromEmail,
                            "to", email,
                            "subject", "Din DreamScope engangskode",
                            "html", "<p>Din DreamScope engangskode er:</p><h1>" + code + "</h1><p>Koden er gyldig i 10 minutter.</p>"
                    ))
                    .retrieve()
                    .toBodilessEntity();
            LOGGER.info("DreamScope one-time code email sent to {} with Resend.", email);
        } catch (RuntimeException exception) {
            LOGGER.warn("Could not send one-time code email to {}", email, exception);
        }
    }

    public void sendDeletionConfirmation(String email) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            LOGGER.info("Account deletion confirmation for {}", email);
            return;
        }

        try {
            restClient.post()
                    .uri("/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + resendApiKey)
                    .body(Map.of(
                            "from", fromEmail,
                            "to", email,
                            "subject", "Din DreamScope konto er slettet",
                            "html", "<p>Din konto og dine drømmedata er slettet fra DreamScope.</p>"
                    ))
                    .retrieve()
                    .toBodilessEntity();
            LOGGER.info("DreamScope deletion confirmation email sent to {} with Resend.", email);
        } catch (RuntimeException exception) {
            LOGGER.warn("Could not send deletion confirmation email to {}", email, exception);
        }
    }
}
