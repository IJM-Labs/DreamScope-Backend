package com.example.dreamscopebackend.dto.response;

import java.time.Instant;
import java.util.UUID;

public record TermsResponseDTO(UUID termsId, String version, String content, Instant createdAt) {
}
