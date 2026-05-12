package com.example.dreamscopebackend.dto.response;

import java.time.Instant;
import java.util.UUID;

public record InterpretationResponseDTO(UUID interpretationId, String text, Instant createdAt) {
}
