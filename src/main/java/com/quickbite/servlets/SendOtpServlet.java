package com.quickbite.servlets;

import com.quickbite.security.OtpService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/auth/send-otp")
public class SendOtpServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json; charset=UTF-8");
        String email = request.getParameter("email");

        if (email == null || email.trim().isEmpty()) {
            response.setStatus(400);
            response.getWriter().write("{\"success\":false,\"error\":\"Email address is required\"}");
            return;
        }
        email = email.trim().toLowerCase();

        // Retrieve SMTP credentials using Config utility
        String mailUser = com.quickbite.security.Config.get("EMAIL_USER");
        String mailPass = com.quickbite.security.Config.get("EMAIL_PASS");

        // Generate 6-digit OTP
        String otpCode = OtpService.generateOtp();
        System.out.println("[SendOtpServlet] Generated OTP for " + email + " is: " + otpCode);

        // Send OTP using OtpService
        boolean sent = OtpService.sendOtpEmail(mailUser, mailPass, email, otpCode);

        if (sent) {
            // Save in Session with 5-minute expiry
            HttpSession session = request.getSession(true);
            session.setAttribute("email_otp", otpCode);
            session.setAttribute("email_otp_target", email);
            session.setAttribute("email_otp_expiry", System.currentTimeMillis() + 5 * 60 * 1000); // 5 mins

            response.getWriter().write("{\"success\":true,\"message\":\"OTP sent successfully to " + email + "\"}");
        } else {
            response.setStatus(500);
            response.getWriter().write("{\"success\":false,\"error\":\"Failed to send verification email. Check server credentials.\"}");
        }
    }
}
