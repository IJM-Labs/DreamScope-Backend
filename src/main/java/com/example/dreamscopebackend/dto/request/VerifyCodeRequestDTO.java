package com.example.dreamscopebackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerifyCodeRequestDTO(

    @NotBlank(message = "Kode må ikke være blank")
    @Size(min = 6, max = 6, message = "Koden skal være præcis 6 tal")
    @Pattern(regexp = "\\d{6}", message = "Koden må kun indeholde tal")
    String code

) {}
