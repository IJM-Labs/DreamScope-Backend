package com.example.dreamscopebackend.service;

import com.example.dreamscopebackend.dto.request.UpdateUserRequestDTO;
import com.example.dreamscopebackend.dto.response.UserResponseDTO;
import com.example.dreamscopebackend.entity.User;
import com.example.dreamscopebackend.exception.UserNotFoundException;
import com.example.dreamscopebackend.repository.DreamRepository;
import com.example.dreamscopebackend.repository.MagicLinkRepository;
import com.example.dreamscopebackend.repository.UserRepository;
import com.example.dreamscopebackend.repository.UserTermsRepository;
import com.example.dreamscopebackend.util.HashUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final DreamRepository dreamRepository;
    private final MagicLinkRepository magicLinkRepository;
    private final UserTermsRepository userTermsRepository;
    private final EncryptionService encryptionService;
    private final MailService mailService;

    public UserService(
            UserRepository userRepository,
            DreamRepository dreamRepository,
            MagicLinkRepository magicLinkRepository,
            UserTermsRepository userTermsRepository,
            EncryptionService encryptionService,
            MailService mailService
    ) {
        this.userRepository = userRepository;
        this.dreamRepository = dreamRepository;
        this.magicLinkRepository = magicLinkRepository;
        this.userTermsRepository = userTermsRepository;
        this.encryptionService = encryptionService;
        this.mailService = mailService;
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getCurrentUser(UUID userId) {
        return toResponse(findUser(userId));
    }

    @Transactional
    public UserResponseDTO updateCurrentUser(UUID userId, UpdateUserRequestDTO request) {
        User user = findUser(userId);
        if (request.email() != null && !request.email().isBlank()) {
            String email = normalizeEmail(request.email());
            String emailHash = HashUtil.sha256(email);
            userRepository.findByEmailHash(emailHash)
                    .filter(existing -> !existing.getUserId().equals(userId))
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Email is already in use");
                    });
            user.setEmailHash(emailHash);
            user.setEmailEncrypted(encryptionService.encrypt(email));
        }
        if (request.nickname() != null && !request.nickname().isBlank()) {
            user.setNicknameEncrypted(encryptionService.encrypt(request.nickname().trim()));
        }
        return toResponse(user);
    }

    @Transactional
    public void deleteCurrentUser(UUID userId) {
        User user = findUser(userId);
        String email = encryptionService.decrypt(user.getEmailEncrypted());
        dreamRepository.deleteByUserUserId(userId);
        magicLinkRepository.deleteByUserUserId(userId);
        userTermsRepository.deleteByUserUserId(userId);
        userRepository.delete(user);
        mailService.sendDeletionConfirmation(email);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User was not found"));
    }

    private UserResponseDTO toResponse(User user) {
        return new UserResponseDTO(
                user.getUserId(),
                encryptionService.decrypt(user.getEmailEncrypted()),
                encryptionService.decrypt(user.getNicknameEncrypted()),
                user.getCreatedAt()
        );
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Email is invalid");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
