package com.example.dreamscopebackend.service;

import com.example.dreamscopebackend.dto.request.AcceptTermsRequestDTO;
import com.example.dreamscopebackend.entity.User;
import com.example.dreamscopebackend.repository.UserRepository;
import com.example.dreamscopebackend.repository.UserTermsRepository;
import com.example.dreamscopebackend.util.HashUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TermsServiceTest {
    @Autowired
    private TermsService termsService;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserTermsRepository userTermsRepository;

    @Test
    void getLatestTermsCreatesDefaultTermsWhenNoneExist() {
        var terms = termsService.getLatestTerms();

        assertThat(terms.version()).isEqualTo("1.0");
        assertThat(terms.content()).contains("krypteret");
    }

    @Test
    void acceptTermsIsIdempotentForUserAndTermsVersion() {
        User user = createUser("terms@example.com");
        var terms = termsService.getLatestTerms();

        termsService.acceptTerms(user.getUserId(), new AcceptTermsRequestDTO(terms.termsId()));
        termsService.acceptTerms(user.getUserId(), new AcceptTermsRequestDTO(terms.termsId()));

        assertThat(userTermsRepository.findAll()).hasSize(1);
        assertThat(userTermsRepository.existsByUserUserIdAndTermsTermsId(user.getUserId(), terms.termsId())).isTrue();
    }

    private User createUser(String email) {
        User user = new User();
        user.setEmailHash(HashUtil.sha256(email));
        user.setEmailEncrypted(encryptionService.encrypt(email));
        user.setNicknameEncrypted(encryptionService.encrypt("Test User"));
        return userRepository.save(user);
    }
}
