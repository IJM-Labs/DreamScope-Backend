package com.example.dreamscopebackend.dto.response;

import java.util.UUID;

public record VerifyResponseDTO(UUID userId, String email, String nickname, String message) {
}
