package com.example.dreamscopebackend.service;

import com.example.dreamscopebackend.dto.request.LoginRequestDTO;
import com.example.dreamscopebackend.entity.MagicLink;
import com.example.dreamscopebackend.entity.User;
import com.example.dreamscopebackend.exception.InvalidTokenException;
import com.example.dreamscopebackend.exception.TokenExpiredException;
import com.example.dreamscopebackend.repository.MagicLinkRepository;
import com.example.dreamscopebackend.repository.UserRepository;
import com.example.dreamscopebackend.util.HashUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AuthServiceTest {
    @Autowired
    private AuthService authService;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MagicLinkRepository magicLinkRepository;

    @Test
    void loginCreatesEncryptedUserWithNicknameAndHashedOneTimeCode() {
        authService.login(new LoginRequestDTO(" USER@Example.com ", "Night Owl"));

        User user = userRepository.findByEmailHash(HashUtil.sha256("user@example.com")).orElseThrow();
        MagicLink magicLink = magicLinkRepository.findAll().getFirst();

        assertThat(user.getEmailEncrypted()).doesNotContain("user@example.com");
        assertThat(encryptionService.decrypt(user.getEmailEncrypted())).isEqualTo("user@example.com");
        assertThat(user.getNicknameEncrypted()).doesNotContain("Night Owl");
        assertThat(encryptionService.decrypt(user.getNicknameEncrypted())).isEqualTo("Night Owl");
        assertThat(magicLink.getTokenHash()).hasSize(64);
        assertThat(magicLink.getTokenHash()).doesNotContain("USER@Example.com");
        assertThat(magicLink.isUsed()).isFalse();
        assertThat(magicLink.getExpiresAt()).isAfter(Instant.now());
        assertThat(magicLink.getExpiresAt()).isBeforeOrEqualTo(Instant.now().plusSeconds(600));
    }

    @Test
    void loginRequiresNicknameWhenCreatingNewUser() {
        assertThatThrownBy(() -> authService.login(new LoginRequestDTO("new@example.com", "")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nickname");
    }

    @Test
    void verifyRejectsUnknownCode() {
        assertThatThrownBy(() -> authService.verify("123456"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Kode er ugyldig");
    }

    @Test
    void verifyRejectsExpiredCode() {
        User user = createUser("expired@example.com", "Expired User");
        MagicLink magicLink = new MagicLink();
        magicLink.setUser(user);
        magicLink.setTokenHash(HashUtil.sha256("123456"));
        magicLink.setExpiresAt(Instant.now().minusSeconds(1));
        magicLinkRepository.save(magicLink);

        assertThatThrownBy(() -> authService.verify("123456"))
                .isInstanceOf(TokenExpiredException.class)
                .hasMessageContaining("Kode er udløbet");
    }

    @Test
    void verifyMarksCodeUsedAndReturnsDecryptedUserWithNickname() {
        User user = createUser("valid@example.com", "Valid User");
        MagicLink magicLink = new MagicLink();
        magicLink.setUser(user);
        magicLink.setTokenHash(HashUtil.sha256("654321"));
        magicLink.setExpiresAt(Instant.now().plusSeconds(60));
        magicLinkRepository.save(magicLink);

        var response = authService.verify("654321");

        assertThat(response.userId()).isEqualTo(user.getUserId());
        assertThat(response.email()).isEqualTo("valid@example.com");
        assertThat(response.nickname()).isEqualTo("Valid User");
        assertThat(magicLinkRepository.findByTokenHash(HashUtil.sha256("654321")).orElseThrow().isUsed()).isTrue();
    }

    private User createUser(String email, String nickname) {
        User user = new User();
        user.setEmailHash(HashUtil.sha256(email));
        user.setEmailEncrypted(encryptionService.encrypt(email));
        user.setNicknameEncrypted(encryptionService.encrypt(nickname));
        return userRepository.save(user);
    }
}
