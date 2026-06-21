package com.quickbite.dao;

import com.quickbite.connection.DBConnection;
import com.quickbite.models.CartItem;
import com.quickbite.models.Order;
import com.quickbite.models.OrderItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDAO {

    public int placeOrder(int userId, int restaurantId, double total, List<CartItem> cartItems) {
        Connection conn = null;
        PreparedStatement orderPs = null;
        PreparedStatement itemPs = null;
        PreparedStatement clearCartPs = null;
        ResultSet generatedKeys = null;
        int orderId = -1;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start Transaction

            // 1. Insert Order
            String orderSql = "INSERT INTO orders (user_id, restaurant_id, total, status) VALUES (?, ?, ?, 'PLACED')";
            orderPs = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            orderPs.setInt(1, userId);
            orderPs.setInt(2, restaurantId);
            orderPs.setDouble(3, total);
            
            if (orderPs.executeUpdate() == 0) {
                throw new Exception("Order placement failed.");
            }

            generatedKeys = orderPs.getGeneratedKeys();
            if (generatedKeys.next()) {
                orderId = generatedKeys.getInt(1);
            } else {
                throw new Exception("Order placement failed, no ID obtained.");
            }

            // 2. Insert Order Items
            String itemSql = "INSERT INTO order_items (order_id, menu_item_id, quantity, price) VALUES (?, ?, ?, ?)";
            itemPs = conn.prepareStatement(itemSql);
            for (CartItem item : cartItems) {
                itemPs.setInt(1, orderId);
                itemPs.setInt(2, item.getMenuItemId());
                itemPs.setInt(3, item.getQuantity());
                itemPs.setDouble(4, item.getPrice());
                itemPs.addBatch();
            }
            itemPs.executeBatch();

            // 3. Clear Shopping Cart
            String clearSql = "DELETE FROM cart WHERE user_id = ?";
            clearCartPs = conn.prepareStatement(clearSql);
            clearCartPs.setInt(1, userId);
            clearCartPs.executeUpdate();

            conn.commit(); // Commit Transaction
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            orderId = -1;
        } finally {
            try { if (generatedKeys != null) generatedKeys.close(); } catch (Exception e) {}
            try { if (orderPs != null) orderPs.close(); } catch (Exception e) {}
            try { if (itemPs != null) itemPs.close(); } catch (Exception e) {}
            try { if (clearCartPs != null) clearCartPs.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
        return orderId;
    }

    public List<Order> getOrdersByUserId(int userId) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, r.name AS restaurant_name FROM orders o " +
                     "JOIN restaurants r ON o.restaurant_id = r.id WHERE o.user_id = ? " +
                     "ORDER BY o.id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = extractOrder(rs);
                    order.setRestaurantName(rs.getString("restaurant_name"));
                    order.setItems(getOrderItems(order.getId(), conn));
                    list.add(order);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Order> getOrdersByOwnerId(int ownerId) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, r.name AS restaurant_name, u.name AS customer_name FROM orders o " +
                     "JOIN restaurants r ON o.restaurant_id = r.id " +
                     "JOIN users u ON o.user_id = u.id " +
                     "WHERE r.owner_id = ? ORDER BY o.id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = extractOrder(rs);
                    order.setRestaurantName(rs.getString("restaurant_name"));
                    order.setCustomerName(rs.getString("customer_name"));
                    order.setItems(getOrderItems(order.getId(), conn));
                    list.add(order);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, r.name AS restaurant_name, u.name AS customer_name FROM orders o " +
                     "JOIN restaurants r ON o.restaurant_id = r.id " +
                     "JOIN users u ON o.user_id = u.id ORDER BY o.id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Order order = extractOrder(rs);
                order.setRestaurantName(rs.getString("restaurant_name"));
                order.setCustomerName(rs.getString("customer_name"));
                order.setItems(getOrderItems(order.getId(), conn));
                list.add(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Order getOrderById(int orderId) {
        String sql = "SELECT o.*, r.name AS restaurant_name FROM orders o " +
                     "JOIN restaurants r ON o.restaurant_id = r.id WHERE o.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = extractOrder(rs);
                    order.setRestaurantName(rs.getString("restaurant_name"));
                    order.setItems(getOrderItems(order.getId(), conn));
                    return order;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Map<String, Object> getAdminStatistics() {
        Map<String, Object> stats = new HashMap<>();
        try (Connection conn = DBConnection.getConnection()) {
            // Total Revenue
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT SUM(total) FROM orders WHERE status = 'DELIVERED'")) {
                stats.put("totalRevenue", rs.next() ? rs.getDouble(1) : 0.0);
            }
            
            // Total Orders
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM orders")) {
                stats.put("totalOrders", rs.next() ? rs.getInt(1) : 0);
            }

            // Total Users
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM users")) {
                stats.put("totalUsers", rs.next() ? rs.getInt(1) : 0);
            }

            // Total Restaurants
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM restaurants")) {
                stats.put("totalRestaurants", rs.next() ? rs.getInt(1) : 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stats;
    }

    private Order extractOrder(ResultSet rs) throws Exception {
        return new Order(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getInt("restaurant_id"),
            rs.getDouble("total"),
            rs.getString("status"),
            rs.getTimestamp("created_at")
        );
    }

    private List<OrderItem> getOrderItems(int orderId, Connection conn) throws Exception {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.*, mi.name AS menu_item_name FROM order_items oi " +
                     "JOIN menu_items mi ON oi.menu_item_id = mi.id WHERE oi.order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem(
                        rs.getInt("id"),
                        rs.getInt("order_id"),
                        rs.getInt("menu_item_id"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                    );
                    item.setMenuItemName(rs.getString("menu_item_name"));
                    items.add(item);
                }
            }
        }
        return items;
    }
}
