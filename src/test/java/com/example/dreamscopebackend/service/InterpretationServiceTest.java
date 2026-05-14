package com.example.dreamscopebackend.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class InterpretationServiceTest {

    @Test
    void interpretRefusesClearlyOffTopicRequests() {
        InterpretationService service = new InterpretationService(RestClient.builder(), "", "gpt-4.1-mini");

        String response = service.interpret("Hvad er opskriften på et spejlæg?");

        assertThat(response).contains("kun hjælpe med drømme");
    }

    @Test
    void interpretAllowsDreamsThatMentionOffTopicWords() {
        InterpretationService service = new InterpretationService(RestClient.builder(), "", "gpt-4.1-mini");

        String response = service.interpret("Jeg drømte at jeg lavede et spejlæg i et mørkt køkken");

        assertThat(response).contains("Demo interpretation");
    }
}
