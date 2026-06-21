package com.quickbite.servlets;

import com.quickbite.dao.OrderDAO;
import com.quickbite.models.Order;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/order-status-sse")
public class OrderStatusSseServlet extends HttpServlet {
    private OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        
        String orderIdStr = request.getParameter("orderId");
        if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
            return;
        }
        
        int orderId;
        try {
            orderId = Integer.parseInt(orderIdStr);
        } catch (NumberFormatException e) {
            return;
        }
        
        PrintWriter writer = response.getWriter();
        String lastStatus = null;
        
        // Loop and stream updates for up to 5 minutes (150 iterations of 2-second sleep)
        for (int i = 0; i < 150; i++) {
            if (writer.checkError()) {
                break; // Client closed connection
            }
            
            Order order = orderDAO.getOrderById(orderId);
            if (order != null) {
                String currentStatus = order.getStatus();
                if (!currentStatus.equals(lastStatus)) {
                    lastStatus = currentStatus;
                    writer.write("data: {\"status\":\"success\",\"orderStatus\":\"" + currentStatus + "\"}\n\n");
                    writer.flush();
                }
            } else {
                writer.write("data: {\"status\":\"error\",\"message\":\"Order not found\"}\n\n");
                writer.flush();
                break;
            }
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
