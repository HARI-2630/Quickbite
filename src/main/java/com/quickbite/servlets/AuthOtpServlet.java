package com.quickbite.servlets;

import com.quickbite.dao.OtpDAO;
import com.quickbite.dao.UserDAO;
import com.quickbite.dao.AuditLogDAO;
import com.quickbite.models.OtpSession;
import com.quickbite.models.User;
import com.quickbite.models.AuditLog;
import com.quickbite.security.JwtUtil;
import com.quickbite.security.SmsEmailService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Random;

@WebServlet("/auth/otp")
public class AuthOtpServlet extends HttpServlet {
    private OtpDAO otpDAO = new OtpDAO();
    private UserDAO userDAO = new UserDAO();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json; charset=UTF-8");
        String action = request.getParameter("action");
        if (action == null) {
            response.setStatus(400);
            response.getWriter().write("{\"success\":false,\"error\":\"Missing action parameter\"}");
            return;
        }

        switch (action) {
            case "sendPhoneOtp":
                handleSendPhoneOtp(request, response);
                break;
            case "verifyPhoneOtp":
                handleVerifyPhoneOtp(request, response);
                break;
            case "verifyGoogleOtp":
                handleVerifyGoogleOtp(request, response);
                break;
            default:
                response.setStatus(400);
                response.getWriter().write("{\"success\":false,\"error\":\"Unknown action\"}");
        }
    }

    private void handleSendPhoneOtp(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String phone = request.getParameter("phone");
        if (phone == null || phone.trim().isEmpty()) {
            response.setStatus(400);
            response.getWriter().write("{\"success\":false,\"error\":\"Phone number is required\"}");
            return;
        }
        phone = phone.trim();

        // Basic E.164 phone validation pattern
        if (!phone.matches("^\\+?[1-9]\\d{1,14}$")) {
            response.setStatus(400);
            response.getWriter().write("{\"success\":false,\"error\":\"Invalid phone number. Use format: +919876543210\"}");
            return;
        }

        OtpSession session = otpDAO.getSession(phone);
        long now = System.currentTimeMillis();
        int resendCount = 1;

        if (session != null) {
            long lastRequest = session.getLastRequestedAt().getTime();
            // Check if within 10 minutes resend window
            if (now - lastRequest < 10 * 60 * 1000) {
                if (session.getResendCount() >= 3) {
                    response.setStatus(429);
                    response.getWriter().write("{\"success\":false,\"error\":\"Max 3 OTP attempts exceeded per 10 minutes. Please try again later.\"}");
                    return;
                }
                resendCount = session.getResendCount() + 1;
            } else {
                resendCount = 1; // Reset limit
            }
        }

        // Generate 6-digit secure-enough numeric OTP code
        String otpCode = String.format("%06d", new Random().nextInt(1000000));
        
        OtpSession newSession = new OtpSession();
        newSession.setPhoneOrEmail(phone);
        newSession.setOtpCode(otpCode);
        newSession.setResendCount(resendCount);
        newSession.setLastRequestedAt(new Timestamp(now));
        newSession.setExpiresAt(new Timestamp(now + 5 * 60 * 1000)); // 5 mins expiration
        newSession.setVerified(false);

        boolean saved = otpDAO.saveSession(newSession);
        if (!saved) {
            response.setStatus(500);
            response.getWriter().write("{\"success\":false,\"error\":\"Internal database error saving verification code\"}");
            return;
        }

        // Send OTP via SmsEmailService (Twilio / fallback logger)
        if (SmsEmailService.isSmsConfigured()) {
            boolean sent = SmsEmailService.sendSmsOtp(phone, otpCode);
            if (sent) {
                response.getWriter().write("{\"success\":true,\"message\":\"OTP sent successfully\"}");
            } else {
                response.setStatus(500);
                response.getWriter().write("{\"success\":false,\"error\":\"Unable to deliver SMS verification code\"}");
            }
        } else {
            System.out.println("[AuthOtpServlet] Twilio is not configured. Returning simulated code: " + otpCode);
            response.getWriter().write("{\"success\":true,\"message\":\"OTP sent successfully (Demo Sandbox Mode)\",\"demoOtp\":\"" + otpCode + "\"}");
        }
    }

    private void handleVerifyPhoneOtp(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String phone = request.getParameter("phone");
        String otp = request.getParameter("otp");

        if (phone == null || otp == null || phone.trim().isEmpty() || otp.trim().isEmpty()) {
            response.setStatus(400);
            response.getWriter().write("{\"success\":false,\"error\":\"Phone number and OTP code are required\"}");
            return;
        }
        phone = phone.trim();
        otp = otp.trim();

        OtpSession session = otpDAO.getSession(phone);
        if (session == null || !session.getOtpCode().equals(otp)) {
            response.setStatus(400);
            response.getWriter().write("{\"success\":false,\"error\":\"Invalid OTP code\"}");
            return;
        }

        if (System.currentTimeMillis() > session.getExpiresAt().getTime()) {
            response.setStatus(400);
            response.getWriter().write("{\"success\":false,\"error\":\"OTP has expired. Please request a new code.\"");
            return;
        }

        // Flag session as verified in database
        session.setVerified(true);
        otpDAO.saveSession(session);

        // Check if user is already registered with this phone number
        User user = userDAO.getUserByPhone(phone);
        if (user != null) {
            if ("BLOCKED".equalsIgnoreCase(user.getStatus())) {
                response.setStatus(403);
                response.getWriter().write("{\"success\":false,\"error\":\"This account has been blocked by administrator.\"}");
                return;
            }
            
            // Login user immediately
            issueJwtCookies(user, response, request);
            writeAudit(user.getId(), "Phone OTP Login Successful", request);

            response.getWriter().write("{\"success\":true,\"registered\":true,\"role\":\"" + user.getRole() + "\"}");
        } else {
            // New user, verified state passed. Frontend will show registration form.
            response.getWriter().write("{\"success\":true,\"registered\":false}");
        }
    }

    private void handleVerifyGoogleOtp(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String email = request.getParameter("email");
        String otp = request.getParameter("otp");

        if (email == null || otp == null || email.trim().isEmpty() || otp.trim().isEmpty()) {
            response.setStatus(400);
            response.getWriter().write("{\"success\":false,\"error\":\"Email and OTP code are required\"}");
            return;
        }
        email = email.trim();
        otp = otp.trim();

        OtpSession session = otpDAO.getSession(email);
        if (session == null || !session.getOtpCode().equals(otp)) {
            response.setStatus(400);
            response.getWriter().write("{\"success\":false,\"error\":\"Invalid OTP code\"}");
            return;
        }

        if (System.currentTimeMillis() > session.getExpiresAt().getTime()) {
            response.setStatus(400);
            response.getWriter().write("{\"success\":false,\"error\":\"OTP code expired. Please restart login.\"");
            return;
        }

        // Code matched, clear active verification state
        otpDAO.deleteSession(email);

        User user = userDAO.getUserByEmail(email);
        if (user == null) {
            response.setStatus(400);
            response.getWriter().write("{\"success\":false,\"error\":\"Associated account not found\"}");
            return;
        }

        if ("BLOCKED".equalsIgnoreCase(user.getStatus())) {
            response.setStatus(403);
            response.getWriter().write("{\"success\":false,\"error\":\"This account has been blocked by administrator.\"}");
            return;
        }

        // Finalize authentication process
        issueJwtCookies(user, response, request);
        writeAudit(user.getId(), "Google OAuth OTP Verification Complete", request);

        response.getWriter().write("{\"success\":true,\"role\":\"" + user.getRole() + "\"}");
    }

    private void issueJwtCookies(User user, HttpServletResponse response, HttpServletRequest request) {
        String accessToken = JwtUtil.generateAccessToken(user);
        String refreshToken = JwtUtil.generateRefreshToken(user);

        Cookie accessCookie = new Cookie("jwt_access", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(request.isSecure());
        accessCookie.setPath("/");
        accessCookie.setMaxAge(15 * 60); // 15 mins
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("jwt_refresh", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(request.isSecure());
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(refreshCookie);
    }

    private void writeAudit(int userId, String action, HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        String userAgent = request.getHeader("User-Agent");

        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        auditLogDAO.insertLog(log);
    }
}
