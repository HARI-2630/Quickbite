package com.quickbite.servlets;

import com.quickbite.dao.CartDAO;
import com.quickbite.dao.OrderDAO;
import com.quickbite.dao.AuditLogDAO;
import com.quickbite.models.CartItem;
import com.quickbite.models.User;
import com.quickbite.models.AuditLog;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.YearMonth;
import java.util.List;

@WebServlet("/checkout/stripe")
public class StripePaymentServlet extends HttpServlet {
    private CartDAO cartDAO = new CartDAO();
    private OrderDAO orderDAO = new OrderDAO();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendError(response, "Unauthorized. Please log in first.");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        
        String restaurantIdStr = request.getParameter("restaurantId");
        String totalStr = request.getParameter("totalPrice");
        String cardNumber = request.getParameter("cardNumber");
        String cardHolder = request.getParameter("cardHolder");
        String cardExpiry = request.getParameter("cardExpiry");
        String cardCvv = request.getParameter("cardCvv");
        
        if (restaurantIdStr == null || totalStr == null || cardNumber == null || 
            cardHolder == null || cardExpiry == null || cardCvv == null) {
            sendError(response, "Missing payment details.");
            return;
        }
        
        // 1. Sanitize card input and perform formatting validations
        cardNumber = cardNumber.replaceAll("\\s|-", "");
        cardCvv = cardCvv.trim();
        cardExpiry = cardExpiry.trim();
        
        // 2. Card Validation (Luhn algorithm & formats)
        if (!validateCardNumber(cardNumber)) {
            sendError(response, "Invalid card number format or failed checksum.");
            return;
        }
        
        if (!validateExpiry(cardExpiry)) {
            sendError(response, "Invalid expiry date or card expired.");
            return;
        }
        
        if (!cardCvv.matches("^\\d{3,4}$")) {
            sendError(response, "Invalid CVV format.");
            return;
        }
        
        if (cardHolder.trim().length() < 3) {
            sendError(response, "Invalid cardholder name.");
            return;
        }
        
        try {
            int restaurantId = Integer.parseInt(restaurantIdStr);
            double total = Double.parseDouble(totalStr);
            
            // Fetch cart items
            List<CartItem> cartItems = cartDAO.getCartByUserId(user.getId());
            if (cartItems.isEmpty()) {
                sendError(response, "Your cart is empty. Cannot process checkout.");
                return;
            }
            
            // 3. Simulate payment authorization delay (1.5 seconds)
            Thread.sleep(1500);
            
            // 4. Place order (Atomic database transaction)
            int orderId = orderDAO.placeOrder(user.getId(), restaurantId, total, cartItems);
            
            if (orderId > -1) {
                // 5. Send order confirmation email asynchronously
                try {
                    com.quickbite.dao.RestaurantDAO rDao = new com.quickbite.dao.RestaurantDAO();
                    com.quickbite.models.Restaurant rest = rDao.getRestaurantById(restaurantId);
                    String restName = rest != null ? rest.getName() : "Campus Kitchen";
                    
                    // Trigger email confirmation in a background thread to prevent blocking client response
                    com.quickbite.security.ThreadPoolManager.submit(() -> {
                        com.quickbite.security.SmsEmailService.sendOrderConfirmationEmail(
                            user.getEmail(), user.getName(), orderId, restName, total, cartItems
                        );
                    });
                } catch (Exception ex) {
                    System.err.println("[StripePaymentServlet] Async email trigger failed: " + ex.getMessage());
                }
                
                // 6. Add Audit Log
                String ipAddress = request.getRemoteAddr();
                String userAgent = request.getHeader("User-Agent");
                AuditLog log = new AuditLog();
                log.setUserId(user.getId());
                log.setAction("STRIPE_CHECKOUT_SUCCESS: Order #" + orderId + ", Amount ₹" + String.format("%.2f", total));
                log.setIpAddress(ipAddress);
                log.setUserAgent(userAgent);
                auditLogDAO.insertLog(log);
                
                // Send success response
                response.setContentType("application/json");
                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"status\":\"success\",\"orderId\":" + orderId + "}");
                }
            } else {
                sendError(response, "Transaction processing failed inside order database transaction.");
            }
            
        } catch (NumberFormatException e) {
            sendError(response, "Invalid price or restaurant reference.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            sendError(response, "Payment processor handshake interrupted.");
        } catch (Exception e) {
            sendError(response, "Internal checkouts error: " + e.getMessage());
        }
    }
    
    // Luhn Algorithm Card Checksum Verification
    public static boolean validateCardNumber(String number) {
        if (number == null) return false;
        number = number.replaceAll("\\s|-", "");
        if (!number.matches("^\\d{13,19}$")) {
            return false;
        }
        
        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }
    
    // Expiry Date Validation
    public static boolean validateExpiry(String expiry) {
        if (expiry == null) return false;
        expiry = expiry.trim();
        if (!expiry.matches("^(0[1-9]|1[0-2])\\/?([0-9]{2})$")) {
            return false;
        }
        
        try {
            String[] parts = expiry.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt("20" + parts[1]);
            
            YearMonth cardYearMonth = YearMonth.of(year, month);
            return cardYearMonth.isAfter(YearMonth.now()) || cardYearMonth.equals(YearMonth.now());
        } catch (Exception e) {
            return false;
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
