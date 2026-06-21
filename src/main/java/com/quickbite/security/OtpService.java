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
        if (mailUser == null || mailUser.isEmpty() || mailPass == null || mailPass.isEmpty()) {
            System.err.println("[OtpService Error] SMTP mailUser or mailPass context parameter is missing!");
            return false;
        }

        // Setup mail server properties
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");

        // Create session with authenticator
        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailUser, mailPass);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailUser, "QuickBite Security"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("QuickBite Security Verification Code");

            String htmlContent = "<html><body style='font-family:sans-serif; background:#0f0f13; color:#f3f4f6; padding:30px;'>"
                    + "<div style='background:#151821; border:1px solid rgba(255,255,255,0.06); padding:24px; border-radius:12px; max-width:400px; margin:0 auto;'>"
                    + "<h2 style='color:#ff5e3a; border-bottom:1px solid rgba(255,255,255,0.05); padding-bottom:12px;'>⚡ QuickBite OTP</h2>"
                    + "<p>Use the following 6-digit verification code to log in:</p>"
                    + "<div style='font-size:2.2rem; font-weight:800; text-align:center; letter-spacing:6px; margin:24px 0; color:#fff; background:rgba(255,94,58,0.08); padding:10px; border-radius:6px; border:1px dashed #ff5e3a;'>" + otpCode + "</div>"
                    + "<p style='font-size:0.85rem; color:#747d8c;'>This code is valid for 5 minutes. Do not share it with anyone.</p>"
                    + "</div></body></html>";

            // MIME multi-part body to provide plain-text fallback (avoids spam filters)
            MimeMultipart multipart = new MimeMultipart("alternative");

            // Text Part
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText("Hello,\n\nYou requested a verification code to access your QuickBite account. Please enter the following 6-digit OTP code to complete your login:\n\n" + otpCode + "\n\nNote: This code expires in 5 minutes.\n\n© 2026 QuickBite.", "utf-8");

            // HTML Part
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");

            multipart.addBodyPart(textPart);
            multipart.addBodyPart(htmlPart);
            message.setContent(multipart);

            // Add priority headers to bypass spam filters
            message.setHeader("X-Priority", "1");
            message.setHeader("X-MSMail-Priority", "High");
            message.setHeader("Importance", "high");

            Transport.send(message);
            System.out.println("[OtpService] OTP email sent successfully to " + toEmail);
            return true;

        } catch (Exception e) {
            System.err.println("[OtpService Error] Failed to send email via Jakarta Mail: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
