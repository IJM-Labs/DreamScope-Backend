package com.example.dreamscopebackend.service;

import com.example.dreamscopebackend.dto.request.CreateDreamRequestDTO;
import com.example.dreamscopebackend.dto.request.UpdateUserRequestDTO;
import com.example.dreamscopebackend.entity.User;
import com.example.dreamscopebackend.repository.DreamRepository;
import com.example.dreamscopebackend.repository.MagicLinkRepository;
import com.example.dreamscopebackend.repository.UserRepository;
import com.example.dreamscopebackend.util.HashUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserServiceTest {
    @Autowired
    private UserService userService;

    @Autowired
    private DreamService dreamService;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DreamRepository dreamRepository;

    @Autowired
    private MagicLinkRepository magicLinkRepository;

    @Test
    void updateCurrentUserReEncryptsEmailAndUpdatesLookupHash() {
        User user = createUser("old@example.com");

        var response = userService.updateCurrentUser(user.getUserId(), new UpdateUserRequestDTO("NEW@Example.com", null));

        User storedUser = userRepository.findById(user.getUserId()).orElseThrow();
        assertThat(response.email()).isEqualTo("new@example.com");
        assertThat(storedUser.getEmailHash()).isEqualTo(HashUtil.sha256("new@example.com"));
        assertThat(storedUser.getEmailEncrypted()).doesNotContain("new@example.com");
        assertThat(encryptionService.decrypt(storedUser.getEmailEncrypted())).isEqualTo("new@example.com");
    }

    @Test
    void updateCurrentUserCanUpdateNicknameWithoutChangingEmail() {
        User user = createUser("nick@example.com");

        var response = userService.updateCurrentUser(user.getUserId(), new UpdateUserRequestDTO(null, "Moon Walker"));

        User storedUser = userRepository.findById(user.getUserId()).orElseThrow();
        assertThat(response.email()).isEqualTo("nick@example.com");
        assertThat(response.nickname()).isEqualTo("Moon Walker");
        assertThat(encryptionService.decrypt(storedUser.getNicknameEncrypted())).isEqualTo("Moon Walker");
    }

    @Test
    void deleteCurrentUserRemovesAllOwnedDataForGdpr() {
        User user = createUser("gdpr@example.com");
        dreamService.createDream(user.getUserId(), new CreateDreamRequestDTO("Slet mig"));

        userService.deleteCurrentUser(user.getUserId());

        assertThat(userRepository.findById(user.getUserId())).isEmpty();
        assertThat(dreamRepository.findAll()).isEmpty();
        assertThat(magicLinkRepository.findAll()).isEmpty();
    }

    private User createUser(String email) {
        User user = new User();
        user.setEmailHash(HashUtil.sha256(email));
        user.setEmailEncrypted(encryptionService.encrypt(email));
        user.setNicknameEncrypted(encryptionService.encrypt("Test User"));
        return userRepository.save(user);
    }
}
