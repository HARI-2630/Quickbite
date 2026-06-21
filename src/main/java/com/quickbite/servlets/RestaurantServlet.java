package com.quickbite.servlets;

import com.quickbite.dao.MenuItemDAO;
import com.quickbite.dao.OrderDAO;
import com.quickbite.dao.RestaurantDAO;
import com.quickbite.models.MenuItem;
import com.quickbite.models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/restaurant")
public class RestaurantServlet extends HttpServlet {
    private RestaurantDAO restDAO = new RestaurantDAO();
    private MenuItemDAO menuDAO = new MenuItemDAO();
    private OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (!"RESTAURANT_ADMIN".equals(user.getRole())) {
            response.sendRedirect("index.jsp");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            response.sendRedirect("restaurant-dashboard.jsp");
            return;
        }

        try {
            switch (action) {
                case "toggleStatus":
                    int restId = Integer.parseInt(request.getParameter("restaurantId"));
                    String status = request.getParameter("status");
                    restDAO.updateRestaurantStatus(restId, status);
                    break;
                case "addMenuItem":
                    MenuItem newItem = new MenuItem();
                    newItem.setRestaurantId(Integer.parseInt(request.getParameter("restaurantId")));
                    newItem.setName(request.getParameter("name"));
                    newItem.setPrice(Double.parseDouble(request.getParameter("price")));
                    newItem.setCategory(request.getParameter("category"));
                    
                    String addImg = request.getParameter("imageUrl");
                    if (addImg == null || addImg.trim().isEmpty()) {
                        addImg = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500";
                    }
                    newItem.setImageUrl(addImg);
                    menuDAO.addMenuItem(newItem);
                    break;
                case "editMenuItem":
                    MenuItem editItem = new MenuItem();
                    editItem.setId(Integer.parseInt(request.getParameter("itemId")));
                    editItem.setName(request.getParameter("name"));
                    editItem.setPrice(Double.parseDouble(request.getParameter("price")));
                    editItem.setCategory(request.getParameter("category"));
                    
                    String editImg = request.getParameter("imageUrl");
                    if (editImg == null || editImg.trim().isEmpty()) {
                        editImg = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500";
                    }
                    editItem.setImageUrl(editImg);
                    menuDAO.updateMenuItem(editItem);
                    break;
                case "deleteMenuItem":
                    int delId = Integer.parseInt(request.getParameter("itemId"));
                    menuDAO.deleteMenuItem(delId);
                    break;
                case "updateOrderStatus":
                    int orderId = Integer.parseInt(request.getParameter("orderId"));
                    String oStatus = request.getParameter("status");
                    orderDAO.updateOrderStatus(orderId, oStatus);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.sendRedirect("restaurant-dashboard.jsp");
    }
}
