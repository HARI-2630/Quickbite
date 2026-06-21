package com.quickbite.security;

import com.quickbite.models.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AuthSecurityTest {

    @Test
    public void testPasswordHashing() {
        String password = "RecruiterSecurePassword2026";
        String hashed = PasswordHash.hash(password);
        
        assertNotNull(hashed);
        assertTrue(hashed.contains(":"), "Hash must contain PBKDF2 iteration indicators");
        
        // Match verification
        assertTrue(PasswordHash.verify(password, hashed));
        // Mismatch check
        assertFalse(PasswordHash.verify("wrongPassword", hashed));
        // Null inputs
        assertFalse(PasswordHash.verify(null, hashed));
        assertFalse(PasswordHash.verify(password, null));
    }

    @Test
    public void testJwtGenerationAndVerification() {
        User user = new User();
        user.setId(99);
        user.setName("Enterprise Recruiter");
        user.setEmail("recruiter@quickbite.com");
        user.setRole("SUPER_ADMIN");

        String accessToken = JwtUtil.generateAccessToken(user);
        assertNotNull(accessToken);
        
        // Validate Token
        JwtUtil.Claims claims = JwtUtil.validateToken(accessToken);
        assertNotNull(claims);
        assertEquals(99, claims.getUserId());
        assertEquals("recruiter@quickbite.com", claims.email);
        assertEquals("SUPER_ADMIN", claims.role);
        
        // Corrupted token check
        String corruptedToken = accessToken + "extraBits";
        assertNull(JwtUtil.validateToken(corruptedToken), "Modified signature tokens must fail validation");
        assertNull(JwtUtil.validateToken("invalid.token.format"));
    }

    @Test
    public void testRateLimiting() {
        String testIp = "192.168.1.100";
        
        // First 5 attempts (bucket capacity) must be allowed
        for (int i = 0; i < 5; i++) {
            assertTrue(RateLimiter.isAllowed(testIp), "Attempt " + (i + 1) + " should be allowed");
        }
        
        // 6th attempt immediately must be throttled
        assertFalse(RateLimiter.isAllowed(testIp), "6th immediate request should be throttled (bucket limit 5)");
    }

    @Test
    public void testCardNumberValidation() {
        // Standard Stripe test cards
        assertTrue(com.quickbite.servlets.StripePaymentServlet.validateCardNumber("4242 4242 4242 4242"));
        assertTrue(com.quickbite.servlets.StripePaymentServlet.validateCardNumber("4242-4242-4242-4242"));
        
        // Invalid Luhn checksum
        assertFalse(com.quickbite.servlets.StripePaymentServlet.validateCardNumber("4242 4242 4242 4243"));
        
        // Invalid lengths or characters
        assertFalse(com.quickbite.servlets.StripePaymentServlet.validateCardNumber("123"));
        assertFalse(com.quickbite.servlets.StripePaymentServlet.validateCardNumber("abcd-efgh-ijkl-mnop"));
        assertFalse(com.quickbite.servlets.StripePaymentServlet.validateCardNumber(null));
    }

    @Test
    public void testExpiryValidation() {
        // Valid future dates
        assertTrue(com.quickbite.servlets.StripePaymentServlet.validateExpiry("12/28"));
        assertTrue(com.quickbite.servlets.StripePaymentServlet.validateExpiry("08 / 30"));
        
        // Invalid formats or expired dates
        assertFalse(com.quickbite.servlets.StripePaymentServlet.validateExpiry("15/28")); // Invalid month
        assertFalse(com.quickbite.servlets.StripePaymentServlet.validateExpiry("05/20")); // Expired
        assertFalse(com.quickbite.servlets.StripePaymentServlet.validateExpiry("invalid"));
        assertFalse(com.quickbite.servlets.StripePaymentServlet.validateExpiry(null));
    }
}
