package com.quickbite.servlets;

import com.quickbite.dao.UserDAO;
import com.quickbite.dao.AuditLogDAO;
import com.quickbite.models.User;
import com.quickbite.models.AuditLog;
import com.quickbite.security.PasswordHash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.util.List;

@WebServlet("/dashboard/profile")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, // 2MB
    maxFileSize = 1024 * 1024 * 10,      // 10MB
    maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class UserDashboardServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/index.jsp?error=login_required");
            return;
        }

        // Retrieve safety and action audits for user
        List<AuditLog> logs = auditLogDAO.getLogsByUserId(user.getId());
        request.setAttribute("auditLogs", logs);

        // Forward execution to JSP view
        request.getRequestDispatcher("/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/index.jsp?error=login_required");
            return;
        }

        String action = request.getParameter("action");
        if ("updateProfile".equals(action)) {
            handleUpdateProfile(request, response, user);
        } else if ("changePassword".equals(action)) {
            handleChangePassword(request, response, user);
        } else {
            response.sendRedirect(request.getContextPath() + "/dashboard/profile");
        }
    }

    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException {
        
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");

        if (name == null || name.trim().isEmpty() || email == null || email.trim().isEmpty()) {
            request.setAttribute("profileError", "Name and email are required fields.");
            doGet(request, response);
            return;
        }

        user.setName(name.trim());
        user.setEmail(email.trim());
        if (phone != null && !phone.trim().isEmpty()) {
            user.setPhone(phone.trim());
        } else {
            user.setPhone(null);
        }

        // Process Multipart avatar upload
        try {
            Part filePart = request.getPart("avatarFile");
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = getFileName(filePart);
                if (fileName != null) {
                    fileName = fileName.toLowerCase();
                    if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".gif")) {
                        String uploadPath = getServletContext().getRealPath("") + File.separator + "assets" + File.separator + "avatars";
                        File uploadDir = new File(uploadPath);
                        if (!uploadDir.exists()) {
                            uploadDir.mkdirs();
                        }
                        
                        String extension = fileName.substring(fileName.lastIndexOf('.'));
                        String newFileName = "user_" + user.getId() + "_" + System.currentTimeMillis() + extension;
                        filePart.write(uploadPath + File.separator + newFileName);
                        
                        user.setAvatarUrl("assets/avatars/" + newFileName);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[UserDashboardServlet] File upload exception: " + e.getMessage());
        }

        boolean success = userDAO.updateUserProfile(user);
        if (success) {
            request.getSession().setAttribute("user", user);
            writeAudit(user.getId(), "Profile Details Updated", request);
            request.setAttribute("profileSuccess", "Profile details updated successfully.");
        } else {
            request.setAttribute("profileError", "Unable to update profile. Email or phone number might already be in use.");
        }
        
        doGet(request, response);
    }

    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException {
        
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (currentPassword == null || newPassword == null || confirmPassword == null ||
            currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            request.setAttribute("passwordError", "All fields are required.");
            doGet(request, response);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("passwordError", "New password and password confirmation fields do not match.");
            doGet(request, response);
            return;
        }

        // Fetch fresh object to verify current password hash
        User freshUser = userDAO.getUserById(user.getId());
        if (!PasswordHash.verify(currentPassword, freshUser.getPassword())) {
            request.setAttribute("passwordError", "Incorrect current password entered.");
            doGet(request, response);
            return;
        }

        // Hash and persist updated password
        String hashedNewPassword = PasswordHash.hash(newPassword);
        boolean success = userDAO.updateUserPassword(user.getId(), hashedNewPassword);
        if (success) {
            writeAudit(user.getId(), "Profile Password Changed", request);
            request.setAttribute("passwordSuccess", "Password changed successfully.");
        } else {
            request.setAttribute("passwordError", "Internal database error updating password.");
        }

        doGet(request, response);
    }

    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                String name = token.substring(token.indexOf("=") + 2, token.length() - 1);
                // Handle IE/Edge browser full path filenames
                return new File(name).getName();
            }
        }
        return null;
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
