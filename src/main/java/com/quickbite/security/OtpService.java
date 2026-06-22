package com.quickbite.security;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.Random;

public class OtpService {

    /**
     * Generates a 6-digit numeric OTP code.
     */
    public static String generateOtp() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    /**
     * Sends an OTP code to a recipient email using Gmail SMTP and Jakarta Mail.
     */
    public static boolean sendOtpEmail(String mailUser, String mailPass, String toEmail, String otpCode) {
        return SmsEmailService.sendEmailOtp(toEmail, otpCode);
    }
}
