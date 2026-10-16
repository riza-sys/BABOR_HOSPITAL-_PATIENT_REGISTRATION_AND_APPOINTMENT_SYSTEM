package com.school.dao;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

final class PasswordUtil {
    private static final int ITERATIONS = 65_536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private PasswordUtil() {
    }

    static String hash(String password) throws GeneralSecurityException {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        byte[] hash = derive(password.toCharArray(), salt);
        return "pbkdf2$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    static boolean verify(String password, String storedValue) throws GeneralSecurityException {
        String[] parts = storedValue.split("\\$");
        if (parts.length != 4 || !parts[0].equals("pbkdf2")) {
            return false;
        }
        int iterations = Integer.parseInt(parts[1]);
        byte[] salt = Base64.getDecoder().decode(parts[2]);
        byte[] expected = Base64.getDecoder().decode(parts[3]);
        byte[] actual = derive(password.toCharArray(), salt, iterations);
        if (actual.length != expected.length) {
            return false;
        }
        int difference = 0;
        for (int i = 0; i < actual.length; i++) {
            difference |= actual[i] ^ expected[i];
        }
        return difference == 0;
    }

    private static byte[] derive(char[] password, byte[] salt) throws GeneralSecurityException {
        return derive(password, salt, ITERATIONS);
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }
}
