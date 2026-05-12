package com.example.dreamscopebackend.controller;

import com.example.dreamscopebackend.dto.request.CreateDreamRequestDTO;
import com.example.dreamscopebackend.dto.response.DreamResponseDTO;
import com.example.dreamscopebackend.service.DreamService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dreams")
public class DreamController {
    private final DreamService dreamService;

    public DreamController(DreamService dreamService) {
        this.dreamService = dreamService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DreamResponseDTO createDream(@RequestBody CreateDreamRequestDTO request, Authentication authentication) {
        return dreamService.createDream(currentUserId(authentication), request);
    }

    @GetMapping
    public List<DreamResponseDTO> getDreams(Authentication authentication) {
        return dreamService.getDreams(currentUserId(authentication));
    }

    @GetMapping("/{id}")
    public DreamResponseDTO getDream(@PathVariable UUID id, Authentication authentication) {
        return dreamService.getDream(currentUserId(authentication), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDream(@PathVariable UUID id, Authentication authentication) {
        dreamService.deleteDream(currentUserId(authentication), id);
    }

    private UUID currentUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
