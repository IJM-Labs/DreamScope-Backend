package com.example.dreamscopebackend.service;

import com.example.dreamscopebackend.dto.request.LoginRequestDTO;
import com.example.dreamscopebackend.dto.response.AuthResponseDTO;
import com.example.dreamscopebackend.dto.response.VerifyResponseDTO;
import com.example.dreamscopebackend.entity.MagicLink;
import com.example.dreamscopebackend.entity.User;
import com.example.dreamscopebackend.exception.InvalidTokenException;
import com.example.dreamscopebackend.exception.TokenExpiredException;
import com.example.dreamscopebackend.repository.MagicLinkRepository;
import com.example.dreamscopebackend.repository.UserRepository;
import com.example.dreamscopebackend.util.DateUtil;
import com.example.dreamscopebackend.util.HashUtil;
import com.example.dreamscopebackend.util.TokenUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final MagicLinkRepository magicLinkRepository;
    private final EncryptionService encryptionService;
    private final MailService mailService;

    public AuthService(
            UserRepository userRepository,
            MagicLinkRepository magicLinkRepository,
            EncryptionService encryptionService,
            MailService mailService
    ) {
        this.userRepository = userRepository;
        this.magicLinkRepository = magicLinkRepository;
        this.encryptionService = encryptionService;
        this.mailService = mailService;
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        String email = normalizeEmail(request.email());
        String emailHash = HashUtil.sha256(email);
        User user = userRepository.findByEmailHash(emailHash).orElseGet(() -> {
            String nickname = normalizeNickname(request.nickname());
            User created = new User();
            created.setEmailHash(emailHash);
            created.setEmailEncrypted(encryptionService.encrypt(email));
            created.setNicknameEncrypted(encryptionService.encrypt(nickname));
            return userRepository.save(created);
        });

        String code = TokenUtil.oneTimeCode();
        MagicLink magicLink = new MagicLink();
        magicLink.setTokenHash(HashUtil.sha256(code));
        magicLink.setExpiresAt(DateUtil.minutesFromNow(10));
        magicLink.setUser(user);
        magicLinkRepository.save(magicLink);

        mailService.sendOneTimeCode(email, code);
        return new AuthResponseDTO("Engangskode sendt, hvis emailen kan modtage post.");
    }

    @Transactional
    public VerifyResponseDTO verify(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidTokenException("Kode mangler");
        }
        MagicLink magicLink = magicLinkRepository.findByTokenHash(HashUtil.sha256(code))
                .orElseThrow(() -> new InvalidTokenException("Kode er ugyldig"));
        if (magicLink.isUsed()) {
            throw new InvalidTokenException("Kode er allerede brugt");
        }
        if (magicLink.getExpiresAt().isBefore(Instant.now())) {
            throw new TokenExpiredException("Kode er udløbet");
        }

        magicLink.setUsed(true);
        User user = magicLink.getUser();
        return new VerifyResponseDTO(
                user.getUserId(),
                encryptionService.decrypt(user.getEmailEncrypted()),
                encryptionService.decrypt(user.getNicknameEncrypted()),
                "Login verificeret"
        );
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Email er ugyldig");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("Nickname mangler");
        }
        return nickname.trim();
    }
}
