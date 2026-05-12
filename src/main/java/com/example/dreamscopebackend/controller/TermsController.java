package com.example.dreamscopebackend.controller;

import com.example.dreamscopebackend.dto.request.AcceptTermsRequestDTO;
import com.example.dreamscopebackend.dto.response.TermsResponseDTO;
import com.example.dreamscopebackend.service.TermsService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/terms")
public class TermsController {
    private final TermsService termsService;

    public TermsController(TermsService termsService) {
        this.termsService = termsService;
    }

    @GetMapping
    public TermsResponseDTO getTerms() {
        return termsService.getLatestTerms();
    }

    @PostMapping("/accept-terms")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptTerms(@RequestBody(required = false) AcceptTermsRequestDTO request, Authentication authentication) {
        termsService.acceptTerms(UUID.fromString(authentication.getName()), request == null ? new AcceptTermsRequestDTO(null) : request);
    }
}
