package com.example.kafkatest.support;

import java.security.SecureRandom;

public class RandomNumberGenerator {
    private static final SecureRandom random = new SecureRandom();

    public static String generateRandom6Numbers() {
        return String.valueOf(100000 + random.nextInt(900000));
    }

    public static String generateRandom4Numbers() {
        return String.valueOf(1000 + random.nextInt(9000));
    }
}
