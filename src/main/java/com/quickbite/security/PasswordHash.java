package com.quickbite.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordHash {
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    /**
     * Hashes a password using PBKDF2 with HmacSHA256.
     * Returns a string in format ITERATIONS:SALT:HASH.
     */
    public static String hash(String password) {
        if (password == null) {
            return null;
        }
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            
            return ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verifies a password against a stored PBKDF2 hash.
     * Supports a fallback plaintext match for legacy DB records.
     */
    public static boolean verify(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }
        
        // If not formatted as PBKDF2, check plaintext (for backward compatibility if any)
        if (!storedHash.contains(":")) {
            return storedHash.equals(password);
        }
        
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 3) {
                return false;
            }
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] hash = Base64.getDecoder().decode(parts[2]);
            
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, hash.length * 8);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] testHash = factory.generateSecret(spec).getEncoded();
            
            int diff = hash.length ^ testHash.length;
            for (int i = 0; i < hash.length && i < testHash.length; i++) {
                diff |= hash[i] ^ testHash[i];
            }
            return diff == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
