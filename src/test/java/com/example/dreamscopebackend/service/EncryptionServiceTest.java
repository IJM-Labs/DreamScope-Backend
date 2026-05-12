package com.example.dreamscopebackend.service;

import com.example.dreamscopebackend.exception.EncryptionException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EncryptionServiceTest {
    private static final String TEST_KEY = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Test
    void encryptReturnsCiphertextThatCanBeDecrypted() {
        EncryptionService encryptionService = new EncryptionService(TEST_KEY);

        String encrypted = encryptionService.encrypt("Jeg drømte om havet");

        assertThat(encrypted).isNotEqualTo("Jeg drømte om havet");
        assertThat(encryptionService.decrypt(encrypted)).isEqualTo("Jeg drømte om havet");
    }

    @Test
    void encryptUsesDifferentIvForSamePlainText() {
        EncryptionService encryptionService = new EncryptionService(TEST_KEY);

        String first = encryptionService.encrypt("samme tekst");
        String second = encryptionService.encrypt("samme tekst");

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void constructorRejectsMissingKey() {
        assertThatThrownBy(() -> new EncryptionService(""))
                .isInstanceOf(EncryptionException.class)
                .hasMessageContaining("Missing dreamscope.encryption-key");
    }
}
