package com.quickbite.security;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.quickbite.models.User;

public class JwtUtil {
    private static final String SECRET = Config.get("JWT_SECRET", "super-secret-key-quickbite-2026-production-ready");
    private static final long ACCESS_TOKEN_EXPIRY_MS = 15 * 60 * 1000; // 15 mins
    private static final long REFRESH_TOKEN_EXPIRY_MS = 7 * 24 * 60 * 60 * 1000; // 7 days

    public static String generateAccessToken(User user) {
        long exp = System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY_MS;
        return createToken(user.getId(), user.getEmail(), user.getRole(), exp);
    }

    public static String generateRefreshToken(User user) {
        long exp = System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY_MS;
        return createToken(user.getId(), user.getEmail(), user.getRole(), exp);
    }

    private static String createToken(int userId, String email, String role, long exp) {
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = "{\"sub\":\"" + userId + "\",\"email\":\"" + email + "\",\"role\":\"" + role + "\",\"exp\":" + exp + "}";
        
        String encodedHeader = base64UrlEncode(header.getBytes(StandardCharsets.UTF_8));
        String encodedPayload = base64UrlEncode(payload.getBytes(StandardCharsets.UTF_8));
        
        String signatureInput = encodedHeader + "." + encodedPayload;
        String signature = hmacSha256(signatureInput, SECRET);
        
        return signatureInput + "." + signature;
    }

    public static Claims validateToken(String token) {
        if (token == null) return null;
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return null;
        }
        
        String encodedHeader = parts[0];
        String encodedPayload = parts[1];
        String signature = parts[2];
        
        // Verify signature
        String signatureInput = encodedHeader + "." + encodedPayload;
        String expectedSignature = hmacSha256(signatureInput, SECRET);
        if (!expectedSignature.equals(signature)) {
            return null; // Invalid signature
        }
        
        // Decode payload
        byte[] payloadBytes = base64UrlDecode(encodedPayload);
        String payload = new String(payloadBytes, StandardCharsets.UTF_8);
        
        // Extract claims
        Claims claims = parseClaims(payload);
        if (claims == null) return null;
        
        // Check expiry
        if (System.currentTimeMillis() > claims.exp) {
            return null; // Expired
        }
        
        return claims;
    }

    private static String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static byte[] base64UrlDecode(String str) {
        return Base64.getUrlDecoder().decode(str);
    }

    private static String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error computing HMAC-SHA256", e);
        }
    }

    private static Claims parseClaims(String json) {
        try {
            Claims claims = new Claims();
            claims.sub = extractJsonField(json, "sub");
            claims.email = extractJsonField(json, "email");
            claims.role = extractJsonField(json, "role");
            String expStr = extractJsonField(json, "exp");
            if (expStr != null) {
                claims.exp = Long.parseLong(expStr);
            }
            return claims;
        } catch (Exception e) {
            return null;
        }
    }

    private static String extractJsonField(String json, String fieldName) {
        String keyPattern = "\"" + fieldName + "\":";
        int startIdx = json.indexOf(keyPattern);
        if (startIdx == -1) {
            return null;
        }
        startIdx += keyPattern.length();
        
        char firstChar = json.charAt(startIdx);
        if (firstChar == '"') {
            int endIdx = json.indexOf('"', startIdx + 1);
            if (endIdx != -1) {
                return json.substring(startIdx + 1, endIdx);
            }
        } else {
            // number or boolean
            int endIdx = json.indexOf(',', startIdx);
            if (endIdx == -1) {
                endIdx = json.indexOf('}', startIdx);
            }
            if (endIdx != -1) {
                return json.substring(startIdx, endIdx).trim();
            }
        }
        return null;
    }

    public static class Claims {
        public String sub;  // User ID
        public String email;
        public String role;
        public long exp;
        
        public int getUserId() {
            return Integer.parseInt(sub);
        }
    }
}
