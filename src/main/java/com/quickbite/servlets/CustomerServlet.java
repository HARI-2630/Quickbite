package com.quickbite.servlets;

import com.quickbite.dao.CartDAO;
import com.quickbite.dao.OrderDAO;
import com.quickbite.models.CartItem;
import com.quickbite.models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/customer")
public class CustomerServlet extends HttpServlet {
    private OrderDAO orderDAO = new OrderDAO();
    private CartDAO cartDAO = new CartDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendError(response, "Unauthorized access.");
            return;
        }

        User user = (User) session.getAttribute("user");
        String action = request.getParameter("action");
        
        if ("placeOrder".equals(action)) {
            handlePlaceOrder(request, response, user);
        } else {
            sendError(response, "Invalid action");
        }
    }

    private void handlePlaceOrder(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        try {
            int restaurantId = Integer.parseInt(request.getParameter("restaurantId"));
            double total = Double.parseDouble(request.getParameter("totalPrice"));

            List<CartItem> cartItems = cartDAO.getCartByUserId(user.getId());
            if (cartItems.isEmpty()) {
                sendError(response, "Cannot place order: Cart is empty.");
                return;
            }

            int orderId = orderDAO.placeOrder(user.getId(), restaurantId, total, cartItems);
            
            if (orderId > -1) {
                // Send confirmation email asynchronously
                try {
                    com.quickbite.dao.RestaurantDAO rDao = new com.quickbite.dao.RestaurantDAO();
                    com.quickbite.models.Restaurant rest = rDao.getRestaurantById(restaurantId);
                    String restName = rest != null ? rest.getName() : "Campus Kitchen";
                    final List<CartItem> cartItemsCopy = cartItems;
                    com.quickbite.security.ThreadPoolManager.submit(() -> {
                        com.quickbite.security.SmsEmailService.sendOrderConfirmationEmail(
                            user.getEmail(), user.getName(), orderId, restName, total, cartItemsCopy
                        );
                    });
                } catch (Exception ex) {
                    System.err.println("[CustomerServlet] Async email trigger failed: " + ex.getMessage());
                }

                // Add Audit Log
                try {
                    com.quickbite.dao.AuditLogDAO auditDao = new com.quickbite.dao.AuditLogDAO();
                    com.quickbite.models.AuditLog log = new com.quickbite.models.AuditLog();
                    log.setUserId(user.getId());
                    log.setAction("ORDER_PLACED_SUCCESS: Order #" + orderId + ", Amount ₹" + String.format("%.2f", total));
                    log.setIpAddress(request.getRemoteAddr());
                    log.setUserAgent(request.getHeader("User-Agent"));
                    auditDao.insertLog(log);
                } catch (Exception ex) {
                    System.err.println("[CustomerServlet] Audit log insertion failed: " + ex.getMessage());
                }

                response.setContentType("application/json");
                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"status\":\"success\",\"orderId\":" + orderId + "}");
                }
            } else {
                sendError(response, "Failed to record order in database transaction.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, "Error placing order: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Simple order retrieval API
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");
        String action = request.getParameter("action");
        
        if ("getOrderStatus".equals(action)) {
            try {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                var order = orderDAO.getOrderById(orderId);
                if (order != null && order.getUserId() == user.getId()) {
                    response.setContentType("application/json");
                    try (PrintWriter out = response.getWriter()) {
                        out.print("{\"status\":\"success\",\"orderStatus\":\"" + order.getStatus() + "\"}");
                    }
                } else {
                    sendError(response, "Order not found.");
                }
            } catch (Exception e) {
                sendError(response, "Invalid orderId");
            }
        }
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        try (PrintWriter out = response.getWriter()) {
            out.print("{\"status\":\"error\",\"message\":\"" + message.replace("\"", "\\\"") + "\"}");
        }
    }
}
