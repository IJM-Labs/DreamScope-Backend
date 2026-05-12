package com.example.dreamscopebackend.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class InterpretationServiceTest {
    @Test
    void interpretReturnsFallbackWhenAiServiceFails() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        InterpretationService service = new InterpretationService(builder, "test-key", "test-model");
        server.expect(requestTo("https://api.openai.com/v1/chat/completions"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        String interpretation = service.interpret("Jeg falder ud af en bygning");

        assertThat(interpretation).contains("Demo-fortolkning");
        server.verify();
    }
}
