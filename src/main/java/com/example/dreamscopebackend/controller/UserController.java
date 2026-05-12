package com.example.dreamscopebackend.controller;

import com.example.dreamscopebackend.dto.request.UpdateUserRequestDTO;
import com.example.dreamscopebackend.dto.response.UserResponseDTO;
import com.example.dreamscopebackend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponseDTO getMe(Authentication authentication) {
        return userService.getCurrentUser(currentUserId(authentication));
    }

    @PutMapping("/me")
    public UserResponseDTO updateMe(@RequestBody UpdateUserRequestDTO request, Authentication authentication) {
        return userService.updateCurrentUser(currentUserId(authentication), request);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMe(Authentication authentication, HttpServletRequest request) {
        userService.deleteCurrentUser(currentUserId(authentication));
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    private UUID currentUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
