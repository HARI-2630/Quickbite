package com.quickbite.servlets;

import com.quickbite.dao.CartDAO;
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

@WebServlet("/cart")
public class CartServlet extends HttpServlet {
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
        if (action == null) {
            sendError(response, "Action parameter is missing.");
            return;
        }

        boolean success = false;
        try {
            switch (action) {
                case "add":
                    int addId = Integer.parseInt(request.getParameter("menuItemId"));
                    int addQty = Integer.parseInt(request.getParameter("quantity"));
                    success = cartDAO.addToCart(user.getId(), addId, addQty);
                    break;
                case "update":
                    int upId = Integer.parseInt(request.getParameter("menuItemId"));
                    int upQty = Integer.parseInt(request.getParameter("quantity"));
                    success = cartDAO.updateCartQuantity(user.getId(), upId, upQty);
                    break;
                case "remove":
                    int rmId = Integer.parseInt(request.getParameter("menuItemId"));
                    success = cartDAO.removeFromCart(user.getId(), rmId);
                    break;
                case "clear":
                    success = cartDAO.clearCart(user.getId());
                    break;
            }
            
            if (success) {
                sendJsonResponse(response, user.getId());
            } else {
                sendError(response, "Failed to perform cart action.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, "Error processing cart action: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendError(response, "Unauthorized access.");
            return;
        }

        User user = (User) session.getAttribute("user");
        sendJsonResponse(response, user.getId());
    }

    private void sendJsonResponse(HttpServletResponse response, int userId) throws IOException {
        List<CartItem> items = cartDAO.getCartByUserId(userId);
        int totalQuantity = items.stream().mapToInt(CartItem::getQuantity).sum();
        double subtotal = items.stream().mapToDouble(CartItem::getTotalPrice).sum();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"status\":\"success\",");
            json.append("\"totalQuantity\":").append(totalQuantity).append(",");
            json.append("\"subtotal\":").append(subtotal).append(",");
            json.append("\"items\":[");
            for (int i = 0; i < items.size(); i++) {
                CartItem item = items.get(i);
                json.append("{");
                json.append("\"id\":").append(item.getId()).append(",");
                json.append("\"menuItemId\":").append(item.getMenuItemId()).append(",");
                json.append("\"name\":\"").append(escapeJson(item.getMenuItemName())).append("\",");
                json.append("\"quantity\":").append(item.getQuantity()).append(",");
                json.append("\"price\":").append(item.getPrice()).append(",");
                json.append("\"totalPrice\":").append(item.getTotalPrice());
                json.append("}");
                if (i < items.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");
            json.append("}");
            out.print(json.toString());
        }
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        try (PrintWriter out = response.getWriter()) {
            out.print("{\"status\":\"error\",\"message\":\"" + escapeJson(message) + "\"}");
        }
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
