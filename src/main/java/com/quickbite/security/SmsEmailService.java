package com.quickbite.security;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class SmsEmailService {

    /**
     * Checks if Twilio keys are configured in the .env file.
     */
    public static boolean isSmsConfigured() {
        String sid = Config.get("TWILIO_ACCOUNT_SID");
        String token = Config.get("TWILIO_AUTH_TOKEN");
        String phone = Config.get("TWILIO_PHONE_NUMBER");
        return sid != null && !sid.isEmpty() && token != null && !token.isEmpty() && phone != null && !phone.isEmpty();
    }

    /**
     * Checks if Gmail SMTP credentials are configured in the .env file.
     */
    public static boolean isMailConfigured() {
        String user = Config.get("EMAIL_USER");
        String pass = Config.get("EMAIL_PASS");
        return user != null && !user.isEmpty() && pass != null && !pass.isEmpty();
    }

    /**
     * Sends an OTP via SMS using Twilio's HTTP REST API.
     */
    public static boolean sendSmsOtp(String phoneNumber, String otpCode) {
        if (!isSmsConfigured()) {
            System.out.println("[SmsEmailService SANDBOX] Phone: " + phoneNumber + " | OTP: " + otpCode);
            return true; 
        }

        try {
            String accountSid = Config.get("TWILIO_ACCOUNT_SID");
            String authToken = Config.get("TWILIO_AUTH_TOKEN");
            String fromPhone = Config.get("TWILIO_PHONE_NUMBER");

            String twilioUrl = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";
            URL url = new URL(twilioUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            
            String auth = accountSid + ":" + authToken;
            String authHeader = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", authHeader);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String body = "To=" + java.net.URLEncoder.encode(phoneNumber, "UTF-8")
                    + "&From=" + java.net.URLEncoder.encode(fromPhone, "UTF-8")
                    + "&Body=" + java.net.URLEncoder.encode("Your QuickBite verification code is: " + otpCode + ". It expires in 5 minutes.", "UTF-8");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            return responseCode >= 200 && responseCode < 300;
        } catch (Exception e) {
            System.err.println("[SmsEmailService Error] SMS send failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Sends an email using Jakarta Mail on SMTP Port 587 with STARTTLS.
     */
    public static boolean sendGmail(String to, String subject, String htmlContent) {
        if (!isMailConfigured()) {
            System.out.println("[SmsEmailService SANDBOX] To: " + to + " | Subject: " + subject);
            return true;
        }

        String mailUser = Config.get("EMAIL_USER");
        String mailPass = Config.get("EMAIL_PASS");

        System.out.println("[SmsEmailService] Attempting to send email to " + to + " via smtp.gmail.com:587 with STARTTLS...");

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        prop.put("mail.smtp.connectiontimeout", "2000"); // 2 seconds connect timeout
        prop.put("mail.smtp.timeout", "2000");           // 2 seconds read timeout

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailUser, mailPass);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailUser, "QuickBite Security"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            // MIME multi-part body to provide plain-text fallback (avoids spam filters)
            MimeMultipart multipart = new MimeMultipart("alternative");

            // Text Part
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText("Hello,\n\nUse the following 6-digit verification code to log in:\n\n" + subject + "\n\nNote: This code expires in 5 minutes.\n\n© 2026 QuickBite.", "utf-8");

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
            System.out.println("[SmsEmailService] Email sent successfully to: " + to);
            return true;
        } catch (Exception e) {
            System.err.println("[SmsEmailService Error] JavaMail SMTP send failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sends an OTP via Email (Gmail/Google Extra Verification).
     */
    public static boolean sendEmailOtp(String email, String otpCode) {
        String html = "<html><body style='font-family:sans-serif; background:#0f0f13; color:#f3f4f6; padding:30px;'>"
                + "<div style='background:#151821; border:1px solid rgba(255,255,255,0.06); padding:24px; border-radius:12px; max-width:400px; margin:0 auto;'>"
                + "<h2 style='color:#ff5e3a; border-bottom:1px solid rgba(255,255,255,0.05); padding-bottom:12px;'>⚡ QuickBite OTP</h2>"
                + "<p>Use the following 6-digit verification code to log in:</p>"
                + "<div style='font-size:2.2rem; font-weight:800; text-align:center; letter-spacing:6px; margin:24px 0; color:#fff; background:rgba(255,94,58,0.08); padding:10px; border-radius:6px; border:1px dashed #ff5e3a;'>" + otpCode + "</div>"
                + "<p style='font-size:0.85rem; color:#747d8c;'>This code is valid for 5 minutes. Do not share it with anyone.</p>"
                + "</div></body></html>";
        return sendGmail(email, "⚡ QuickBite Verification OTP", html);
    }

    /**
     * Sends a welcome email after signing up.
     */
    public static boolean sendWelcomeEmail(String email, String name) {
        String html = "<html><body style='font-family:sans-serif; background:#0f0f13; color:#f3f4f6; padding:30px;'>"
                + "<div style='background:#151821; border:1px solid rgba(255,255,255,0.06); padding:24px; border-radius:12px; max-width:400px; margin:0 auto;'>"
                + "<h2 style='color:#ff5e3a;'>🎉 Welcome to QuickBite!</h2>"
                + "<p>Hello " + name + ",</p>"
                + "<p>We are thrilled to welcome you to QuickBite. Start ordering delicious meals from your favorite campus partners now!</p>"
                + "</div></body></html>";
        return sendGmail(email, "Welcome to QuickBite!", html);
    }

    /**
     * Sends a password reset link email.
     */
    public static boolean sendResetPasswordEmail(String email, String resetLink) {
        String html = "<html><body style='font-family:sans-serif; background:#0f0f13; color:#f3f4f6; padding:30px;'>"
                + "<div style='background:#151821; border:1px solid rgba(255,255,255,0.06); padding:24px; border-radius:12px; max-width:400px; margin:0 auto;'>"
                + "<h2 style='color:#ff5e3a;'>🔑 Password Reset Request</h2>"
                + "<p>Click the link below to reset your account password. This link is valid for 15 minutes.</p>"
                + "<p><a href='" + resetLink + "' style='display:inline-block; padding:10px 20px; background:#ff5e3a; color:white; text-decoration:none; border-radius:6px;'>Reset Password</a></p>"
                + "</div></body></html>";
        return sendGmail(email, "🔑 Password Reset Request", html);
    }

    /**
     * Sends a transactional order confirmation email.
     */
    public static boolean sendOrderConfirmationEmail(String toEmail, String userName, int orderId, String restaurantName, double total, java.util.List<com.quickbite.models.CartItem> items) {
        StringBuilder itemsHtml = new StringBuilder();
        for (com.quickbite.models.CartItem item : items) {
            itemsHtml.append("<tr style='border-bottom:1px solid rgba(255,255,255,0.05);'>")
                     .append("<td style='padding:8px 0; color:#f3f4f6;'>").append(item.getMenuItemName()).append("</td>")
                     .append("<td style='padding:8px 0; text-align:center; color:#747d8c;'>x").append(item.getQuantity()).append("</td>")
                     .append("<td style='padding:8px 0; text-align:right; color:#ff5e3a;'>₹").append(String.format("%.2f", item.getTotalPrice())).append("</td>")
                     .append("</tr>");
        }

        String html = "<html><body style='font-family:sans-serif; background:#0f0f13; color:#f3f4f6; padding:30px;'>"
                + "<div style='background:#151821; border:1px solid rgba(255,255,255,0.06); padding:24px; border-radius:12px; max-width:480px; margin:0 auto;'>"
                + "<h2 style='color:#ff5e3a; margin-top:0; border-bottom:1px solid rgba(255,255,255,0.05); padding-bottom:12px;'>⚡ QuickBite Order Placed</h2>"
                + "<p>Hello <strong>" + userName + "</strong>,</p>"
                + "<p>Thank you for ordering! Your order has been successfully placed at <strong>" + restaurantName + "</strong>.</p>"
                + "<div style='margin:20px 0; background:rgba(255,255,255,0.02); border:1px solid rgba(255,255,255,0.04); border-radius:8px; padding:16px;'>"
                + "<div style='margin-bottom:12px; font-size:0.9rem; color:#747d8c;'>"
                + "<span>Order ID: #<strong>" + orderId + "</strong></span>"
                + "</div>"
                + "<table style='width:100%; border-collapse:collapse;'>"
                + "<thead><tr style='border-bottom:1px solid rgba(255,255,255,0.1);'><th style='text-align:left; padding-bottom:8px; color:#747d8c;'>Item</th><th style='text-align:center; padding-bottom:8px; color:#747d8c;'>Qty</th><th style='text-align:right; padding-bottom:8px; color:#747d8c;'>Price</th></tr></thead>"
                + "<tbody>" + itemsHtml.toString() + "</tbody>"
                + "</table>"
                + "<div style='margin-top:16px; border-top:1px dashed rgba(255,255,255,0.1); padding-top:12px; display:flex; justify-content:space-between; align-items:center; font-weight:bold;'>"
                + "<span style='color:#f3f4f6;'>Grand Total:</span>"
                + "<span style='color:#ff5e3a; font-size:1.15rem;'>₹" + String.format("%.2f", total) + "</span>"
                + "</div>"
                + "</div>"
                + "<p style='font-size:0.85rem; color:#747d8c;'>Track your order live on your dashboard or click below:</p>"
                + "<p style='text-align:center; margin-top:20px;'><a href='http://localhost:8080/quickbite/order-tracking.jsp?orderId=" + orderId + "' style='display:inline-block; padding:10px 24px; background:#ff5e3a; color:white; text-decoration:none; border-radius:6px; font-weight:bold;'>Track My Order Live</a></p>"
                + "</div></body></html>";

        return sendGmail(toEmail, "⚡ QuickBite Order Placed #" + orderId, html);
    }
}
