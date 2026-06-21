package com.quickbite.servlets;

import com.quickbite.dao.RestaurantDAO;
import com.quickbite.dao.UserDAO;
import com.quickbite.models.Restaurant;
import com.quickbite.models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private RestaurantDAO restDAO = new RestaurantDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (!"SUPER_ADMIN".equals(user.getRole())) {
            response.sendRedirect("index.jsp");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            response.sendRedirect("admin-dashboard.jsp");
            return;
        }

        try {
            switch (action) {
                case "deleteUser":
                    int delUserId = Integer.parseInt(request.getParameter("userId"));
                    userDAO.deleteUser(delUserId);
                    break;
                case "updateUserRole":
                    int roleUserId = Integer.parseInt(request.getParameter("userId"));
                    String newRole = request.getParameter("role");
                    userDAO.updateUserRole(roleUserId, newRole);
                    break;
                case "addRestaurant":
                    Restaurant rest = new Restaurant();
                    rest.setName(request.getParameter("name"));
                    rest.setCuisine(request.getParameter("cuisine"));
                    rest.setOwnerId(Integer.parseInt(request.getParameter("ownerId")));
                    rest.setStatus("OPEN");
                    restDAO.addRestaurant(rest);
                    break;
                case "deleteRestaurant":
                    int delRestId = Integer.parseInt(request.getParameter("restaurantId"));
                    restDAO.deleteRestaurant(delRestId);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.sendRedirect("admin-dashboard.jsp");
    }
}
