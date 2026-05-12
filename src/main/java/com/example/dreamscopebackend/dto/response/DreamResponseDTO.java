package com.example.dreamscopebackend.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DreamResponseDTO(
        UUID dreamId,
        String content,
        Instant createdAt,
        List<InterpretationResponseDTO> interpretations
) {
}
