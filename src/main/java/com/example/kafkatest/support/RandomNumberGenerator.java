package com.example.kafkatest.support;

import java.security.SecureRandom;

public class RandomNumberGenerator {
    private static final SecureRandom random = new SecureRandom();

    public static String generateRandom6Numbers() {
        return String.valueOf(100000 + random.nextInt(900000));
    }
}
