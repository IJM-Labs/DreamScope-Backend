package com.example.dreamscopebackend.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserResponseDTO(UUID userId, String email, String nickname, Instant createdAt) {
}
