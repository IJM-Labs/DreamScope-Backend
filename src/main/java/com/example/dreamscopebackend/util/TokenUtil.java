package com.example.dreamscopebackend.util;

import java.security.SecureRandom;
import java.util.Base64;

public final class TokenUtil {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private TokenUtil() {
    }

    public static String secureToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String oneTimeCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }
}
