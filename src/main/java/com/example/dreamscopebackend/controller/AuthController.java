package com.example.dreamscopebackend.controller;

import com.example.dreamscopebackend.dto.request.LoginRequestDTO;
import com.example.dreamscopebackend.dto.request.VerifyCodeRequestDTO;
import com.example.dreamscopebackend.dto.response.AuthResponseDTO;
import com.example.dreamscopebackend.dto.response.VerifyResponseDTO;
import com.example.dreamscopebackend.security.SessionAuthenticationFilter;
import com.example.dreamscopebackend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }

      @PostMapping("/verify")
      public VerifyResponseDTO verify(@RequestBody VerifyCodeRequestDTO request,
                                HttpServletRequest request) {

        VerifyResponseDTO response = authService.verify(request.code());

        request.changeSessionId();

        request.getSession().setAttribute(SessionAuthenticationFilter.SESSION_USER_ID,
            response.userId().toString()
    );

    return response;
}

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(Map.of("message", "Logget ud"));
    }
}
