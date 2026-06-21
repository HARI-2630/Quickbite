package com.quickbite.servlets;

import com.quickbite.dao.UserDAO;
import com.quickbite.dao.AuditLogDAO;
import com.quickbite.models.User;
import com.quickbite.models.AuditLog;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/users")
public class AdminUserServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        User currentAdmin = (session != null) ? (User) session.getAttribute("user") : null;
        
        // Ensure request is made by a verified Super Admin
        if (currentAdmin == null || !"SUPER_ADMIN".equalsIgnoreCase(currentAdmin.getRole())) {
            response.sendRedirect(request.getContextPath() + "/index.jsp?error=unauthorized");
            return;
        }

        String action = request.getParameter("action");
        if (action != null) {
            handlePostActions(request, response, currentAdmin);
            return;
        }

        // Fetch users list for view mapping
        List<User> users = userDAO.getAllUsers();
        request.setAttribute("userList", users);

        // Forward to administrator panel view
        request.getRequestDispatcher("/admin-users.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        User currentAdmin = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (currentAdmin == null || !"SUPER_ADMIN".equalsIgnoreCase(currentAdmin.getRole())) {
            response.sendRedirect(request.getContextPath() + "/index.jsp?error=unauthorized");
            return;
        }

        handlePostActions(request, response, currentAdmin);
    }

    private void handlePostActions(HttpServletRequest request, HttpServletResponse response, User currentAdmin) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        String userIdStr = request.getParameter("userId");
        if (userIdStr == null || userIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/users");
            return;
        }

        int targetUserId = Integer.parseInt(userIdStr);

        // Safeguard to prevent locking out own admin access
        if (targetUserId == currentAdmin.getId() && ("delete".equals(action) || "toggleBlock".equals(action) || "updateRole".equals(action))) {
            request.setAttribute("adminError", "For safety, administrative rules prohibit self-deletion or self-blocking.");
            doGet(request, response);
            return;
        }

        boolean success = false;
        String logMessage = "";

        if ("updateRole".equals(action)) {
            String role = request.getParameter("role");
            if (role != null && (role.equals("CUSTOMER") || role.equals("RESTAURANT_ADMIN") || role.equals("SUPER_ADMIN"))) {
                success = userDAO.updateUserRole(targetUserId, role);
                logMessage = "Admin updated User ID " + targetUserId + " role to " + role;
            }
        } else if ("toggleBlock".equals(action)) {
            User targetUser = userDAO.getUserById(targetUserId);
            if (targetUser != null) {
                String newStatus = "ACTIVE".equalsIgnoreCase(targetUser.getStatus()) ? "BLOCKED" : "ACTIVE";
                success = userDAO.updateUserStatus(targetUserId, newStatus);
                logMessage = "Admin updated User ID " + targetUserId + " status to " + newStatus;
            }
        } else if ("delete".equals(action)) {
            success = userDAO.deleteUser(targetUserId);
            logMessage = "Admin deleted User ID " + targetUserId;
        }

        if (success) {
            writeAudit(currentAdmin.getId(), logMessage, request);
            request.setAttribute("adminSuccess", "User details modified successfully.");
        } else {
            request.setAttribute("adminError", "Unable to execute requested admin action.");
        }

        doGet(request, response);
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
