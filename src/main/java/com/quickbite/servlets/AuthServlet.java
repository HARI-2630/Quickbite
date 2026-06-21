package com.quickbite.servlets;

import com.quickbite.dao.UserDAO;
import com.quickbite.dao.OtpDAO;
import com.quickbite.dao.AuditLogDAO;
import com.quickbite.models.User;
import com.quickbite.models.OtpSession;
import com.quickbite.models.AuditLog;
import com.quickbite.security.JwtUtil;
import com.quickbite.security.PasswordHash;
import com.quickbite.security.SmsEmailService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.UUID;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private OtpDAO otpDAO = new OtpDAO();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        switch (action) {
            case "login":
                handleLogin(request, response);
                break;
            case "register":
                handleRegister(request, response);
                break;
            case "forgotPassword":
                handleForgotPassword(request, response);
                break;
            case "resetPassword":
                handleResetPassword(request, response);
                break;
            default:
                response.sendRedirect("index.jsp");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("logout".equals(action)) {
            handleLogout(request, response);
        } else if ("resetPasswordForm".equals(action)) {
            String token = request.getParameter("token");
            String email = request.getParameter("email");
            request.setAttribute("resetToken", token);
            request.setAttribute("resetEmail", email);
            request.getRequestDispatcher("/index.jsp?action=resetPasswordForm").forward(request, response);
        } else {
            response.sendRedirect("index.jsp");
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        User user = userDAO.validateLogin(email, password);
        if (user != null) {
            if ("BLOCKED".equalsIgnoreCase(user.getStatus())) {
                response.sendRedirect("index.jsp?error=blocked");
                return;
            }

            // Issue Secure JWT session cookies
            issueJwtCookies(user, response, request);

            // Log activity audit
            writeAudit(user.getId(), "Email Password Login Successful", request);

            // Redirect based on role
            redirectByRole(user, response);
        } else {
            // Log failed authentication attempt
            writeAudit(null, "Failed Login Attempt for Email: " + email, request);
            response.sendRedirect("index.jsp?error=invalid_credentials");
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String role = request.getParameter("role");
        String phone = request.getParameter("phone");

        if (role == null || (!role.equals("CUSTOMER") && !role.equals("RESTAURANT_ADMIN"))) {
            role = "CUSTOMER"; 
        }

        // Validate that user doesn't already exist
        if (userDAO.getUserByEmail(email) != null) {
            response.sendRedirect("index.jsp?error=email_exists");
            return;
        }

        if (phone != null && !phone.trim().isEmpty()) {
            phone = phone.trim();
            if (userDAO.getUserByPhone(phone) != null) {
                response.sendRedirect("index.jsp?error=phone_exists");
                return;
            }
            
            // Enforce that Phone Login signup requires verified OTP session
            OtpSession otpSession = otpDAO.getSession(phone);
            if (otpSession == null || !otpSession.isVerified()) {
                response.sendRedirect("index.jsp?error=phone_unverified");
                return;
            }
            // Clean up the verified OTP session record
            otpDAO.deleteSession(phone);
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(PasswordHash.hash(password)); // PBKDF2 hash
        user.setRole(role);
        user.setPhone(phone);
        user.setStatus("ACTIVE");

        boolean success = userDAO.registerUser(user);
        if (success) {
            User registeredUser = userDAO.validateLogin(email, password);
            if (registeredUser != null) {
                issueJwtCookies(registeredUser, response, request);
                writeAudit(registeredUser.getId(), "User Registration Successful", request);
                SmsEmailService.sendWelcomeEmail(email, name);
                redirectByRole(registeredUser, response);
            } else {
                response.sendRedirect("index.jsp?msg=registration_success");
            }
        } else {
            response.sendRedirect("index.jsp?error=registration_failed");
        }
    }

    private void handleForgotPassword(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String email = request.getParameter("email");
        if (email == null || email.trim().isEmpty()) {
            response.sendRedirect("index.jsp?error=email_required");
            return;
        }
        email = email.trim();

        User user = userDAO.getUserByEmail(email);
        if (user != null) {
            // Generate single-use password reset token
            String token = UUID.randomUUID().toString();
            long now = System.currentTimeMillis();

            OtpSession session = new OtpSession();
            session.setPhoneOrEmail("reset:" + email);
            session.setOtpCode(token);
            session.setResendCount(1);
            session.setLastRequestedAt(new Timestamp(now));
            session.setExpiresAt(new Timestamp(now + 15 * 60 * 1000)); // 15 mins expiration
            session.setVerified(false);

            otpDAO.saveSession(session);

            String requestUrl = request.getRequestURL().toString();
            String resetLink = requestUrl + "?action=resetPasswordForm&token=" + token + "&email=" + java.net.URLEncoder.encode(email, "UTF-8");
            
            SmsEmailService.sendResetPasswordEmail(email, resetLink);
            writeAudit(user.getId(), "Password Reset Requested", request);
        }

        response.sendRedirect("index.jsp?msg=reset_link_sent");
    }

    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String email = request.getParameter("email");
        String token = request.getParameter("token");
        String newPassword = request.getParameter("newPassword");

        if (email == null || token == null || newPassword == null || email.isEmpty() || token.isEmpty() || newPassword.isEmpty()) {
            response.sendRedirect("index.jsp?error=reset_invalid_parameters");
            return;
        }

        OtpSession session = otpDAO.getSession("reset:" + email);
        if (session == null || !session.getOtpCode().equals(token)) {
            response.sendRedirect("index.jsp?error=reset_invalid_token");
            return;
        }

        if (System.currentTimeMillis() > session.getExpiresAt().getTime()) {
            response.sendRedirect("index.jsp?error=reset_token_expired");
            return;
        }

        User user = userDAO.getUserByEmail(email);
        if (user != null) {
            String hashedNewPassword = PasswordHash.hash(newPassword);
            boolean updated = userDAO.updateUserPassword(user.getId(), hashedNewPassword);
            if (updated) {
                otpDAO.deleteSession("reset:" + email);
                writeAudit(user.getId(), "Password Reset Successful via Token", request);
                response.sendRedirect("index.jsp?msg=password_reset_success");
                return;
            }
        }
        response.sendRedirect("index.jsp?error=reset_failed");
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                writeAudit(user.getId(), "User Logout Successful", request);
            }
            session.invalidate();
        }

        // Clear access and refresh cookies
        Cookie accessCookie = new Cookie("jwt_access", "");
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("jwt_refresh", "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);

        response.sendRedirect("index.jsp?msg=logged_out");
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

    private void redirectByRole(User user, HttpServletResponse response) throws IOException {
        if ("CUSTOMER".equals(user.getRole())) {
            response.sendRedirect("customer-dashboard.jsp");
        } else if ("RESTAURANT_ADMIN".equals(user.getRole())) {
            response.sendRedirect("restaurant-dashboard.jsp");
        } else if ("SUPER_ADMIN".equals(user.getRole())) {
            response.sendRedirect("admin-dashboard.jsp");
        } else {
            response.sendRedirect("index.jsp");
        }
    }

    private void writeAudit(Integer userId, String action, HttpServletRequest request) {
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
