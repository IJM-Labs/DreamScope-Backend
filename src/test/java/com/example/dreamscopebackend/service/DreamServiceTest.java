package com.example.dreamscopebackend.service;

import com.example.dreamscopebackend.dto.request.CreateDreamRequestDTO;
import com.example.dreamscopebackend.entity.Dream;
import com.example.dreamscopebackend.entity.User;
import com.example.dreamscopebackend.repository.DreamRepository;
import com.example.dreamscopebackend.repository.InterpretationRepository;
import com.example.dreamscopebackend.repository.UserRepository;
import com.example.dreamscopebackend.util.HashUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DreamServiceTest {
    @Autowired
    private DreamService dreamService;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DreamRepository dreamRepository;

    @Autowired
    private InterpretationRepository interpretationRepository;

    @Test
    void createDreamStoresDreamAndInterpretationEncryptedButReturnsPlainText() {
        User user = createUser("dreamer@example.com");

        var response = dreamService.createDream(
                user.getUserId(),
                new CreateDreamRequestDTO("Jeg flyver over en by")
        );

        Dream storedDream = dreamRepository.findById(response.dreamId()).orElseThrow();

        assertThat(response.content()).isEqualTo("Jeg flyver over en by");
        assertThat(response.title()).isNotBlank();
        assertThat(response.interpretations()).hasSize(1);
        assertThat(response.interpretations().getFirst().text()).contains("Demo interpretation");
        assertThat(storedDream.getContentEncrypted()).doesNotContain("Jeg flyver over en by");
        assertThat(encryptionService.decrypt(storedDream.getContentEncrypted())).isEqualTo("Jeg flyver over en by");
        assertThat(storedDream.getInterpretations().getFirst().getTextEncrypted()).doesNotContain("Demo interpretation");
    }

    @Test
    void deleteDreamDeletesInterpretationsThroughCascade() {
        User user = createUser("delete-dream@example.com");
        var response = dreamService.createDream(user.getUserId(), new CreateDreamRequestDTO("En dør åbner sig"));

        dreamService.deleteDream(user.getUserId(), response.dreamId());

        assertThat(dreamRepository.findById(response.dreamId())).isEmpty();
        assertThat(interpretationRepository.findAll()).isEmpty();
    }

    private User createUser(String email) {
        User user = new User();
        user.setEmailHash(HashUtil.sha256(email));
        user.setEmailEncrypted(encryptionService.encrypt(email));
        user.setNicknameEncrypted(encryptionService.encrypt("Test User"));
        return userRepository.save(user);
    }
}
