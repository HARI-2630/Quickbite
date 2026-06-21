package com.quickbite.dao;

import com.quickbite.connection.DBConnection;
import com.quickbite.models.CartItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {

    public List<CartItem> getCartByUserId(int userId) {
        List<CartItem> list = new ArrayList<>();
        String sql = "SELECT c.*, m.name AS menu_item_name, m.price FROM cart c " +
                     "JOIN menu_items m ON c.menu_item_id = m.id WHERE c.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("menu_item_id"),
                        rs.getInt("quantity")
                    );
                    item.setMenuItemName(rs.getString("menu_item_name"));
                    item.setPrice(rs.getDouble("price"));
                    list.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addToCart(int userId, int menuItemId, int quantity) {
        // First check if it already exists in user's cart
        String selectSql = "SELECT * FROM cart WHERE user_id = ? AND menu_item_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            boolean exists = false;
            int existingQty = 0;
            
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setInt(1, userId);
                ps.setInt(2, menuItemId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        exists = true;
                        existingQty = rs.getInt("quantity");
                    }
                }
            }
            
            if (exists) {
                String updateSql = "UPDATE cart SET quantity = ? WHERE user_id = ? AND menu_item_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, existingQty + quantity);
                    ps.setInt(2, userId);
                    ps.setInt(3, menuItemId);
                    return ps.executeUpdate() > 0;
                }
            } else {
                String insertSql = "INSERT INTO cart (user_id, menu_item_id, quantity) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, menuItemId);
                    ps.setInt(3, quantity);
                    return ps.executeUpdate() > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateCartQuantity(int userId, int menuItemId, int quantity) {
        String sql = "UPDATE cart SET quantity = ? WHERE user_id = ? AND menu_item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, userId);
            ps.setInt(3, menuItemId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeFromCart(int userId, int menuItemId) {
        String sql = "DELETE FROM cart WHERE user_id = ? AND menu_item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, menuItemId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean clearCart(int userId) {
        String sql = "DELETE FROM cart WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
