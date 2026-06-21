package com.quickbite.servlets;

import com.quickbite.dao.UserDAO;
import com.quickbite.dao.AuditLogDAO;
import com.quickbite.models.User;
import com.quickbite.models.AuditLog;
import com.quickbite.security.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/auth/verify-otp")
public class VerifyOtpServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json; charset=UTF-8");
        String email = request.getParameter("email");
        String otp = request.getParameter("otp");

        if (email == null || otp == null || email.trim().isEmpty() || otp.trim().isEmpty()) {
            response.setStatus(400);
            response.getWriter().write("{\"success\":false,\"error\":\"Email and OTP code are required\"}");
            return;
        }
        email = email.trim().toLowerCase();
        otp = otp.trim();

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(400);
            response.getWriter().write("{\"success\":false,\"error\":\"Session expired or invalid. Please request a new OTP.\"}");
            return;
        }

        String cachedOtp = (String) session.getAttribute("email_otp");
        String cachedTarget = (String) session.getAttribute("email_otp_target");
        Long cachedExpiry = (Long) session.getAttribute("email_otp_expiry");

        if (cachedOtp == null || cachedTarget == null || cachedExpiry == null) {
            response.setStatus(400);
            response.getWriter().write("{\"success\":false,\"error\":\"No active OTP session found. Please request a new OTP.\"}");
            return;
        }

        // Validate matches
        if (!cachedTarget.equals(email)) {
            response.setStatus(400);
            response.getWriter().write("{\"success\":false,\"error\":\"Invalid session mapping for this email address\"}");
            return;
        }

        if (!cachedOtp.equals(otp)) {
            response.setStatus(400);
            response.getWriter().write("{\"success\":false,\"error\":\"Invalid OTP code entered\"}");
            return;
        }

        // Validate expiry
        if (System.currentTimeMillis() > cachedExpiry) {
            session.removeAttribute("email_otp");
            session.removeAttribute("email_otp_target");
            session.removeAttribute("email_otp_expiry");
            response.setStatus(400);
            response.getWriter().write("{\"success\":false,\"error\":\"OTP code has expired. Please request a new OTP.\"}");
            return;
        }

        // Successfully verified, clean session keys
        session.removeAttribute("email_otp");
        session.removeAttribute("email_otp_target");
        session.removeAttribute("email_otp_expiry");

        // Authenticate User or flag for registration
        User user = userDAO.getUserByEmail(email);
        if (user != null) {
            if ("BLOCKED".equalsIgnoreCase(user.getStatus())) {
                response.setStatus(403);
                response.getWriter().write("{\"success\":false,\"error\":\"This account has been blocked by administrator.\"}");
                return;
            }

            // Issue JWT Cookies
            issueJwtCookies(user, response, request);
            writeAudit(user.getId(), "Email OTP Login Successful", request);

            response.getWriter().write("{\"success\":true,\"registered\":true,\"role\":\"" + user.getRole() + "\"}");
        } else {
            // Unregistered user, OTP verification success lets them signup
            response.getWriter().write("{\"success\":true,\"registered\":false}");
        }
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
