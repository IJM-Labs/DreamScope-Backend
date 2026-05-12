package com.example.dreamscopebackend.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public final class DateUtil {
    private DateUtil() {
    }

    public static Instant minutesFromNow(long minutes) {
        return Instant.now().plus(minutes, ChronoUnit.MINUTES);
    }
}
