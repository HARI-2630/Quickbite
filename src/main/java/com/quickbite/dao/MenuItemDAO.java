package com.quickbite.dao;

import com.quickbite.connection.DBConnection;
import com.quickbite.models.MenuItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MenuItemDAO {

    public boolean addMenuItem(MenuItem item) {
        String sql = "INSERT INTO menu_items (restaurant_id, name, price, category, image_url) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getRestaurantId());
            ps.setString(2, item.getName());
            ps.setDouble(3, item.getPrice());
            ps.setString(4, item.getCategory());
            ps.setString(5, item.getImageUrl());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<MenuItem> getMenuItemsByRestaurant(int restaurantId) {
        List<MenuItem> list = new ArrayList<>();
        String sql = "SELECT * FROM menu_items WHERE restaurant_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new MenuItem(
                        rs.getInt("id"),
                        rs.getInt("restaurant_id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("category"),
                        rs.getString("image_url")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public MenuItem getMenuItemById(int id) {
        String sql = "SELECT * FROM menu_items WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new MenuItem(
                        rs.getInt("id"),
                        rs.getInt("restaurant_id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("category"),
                        rs.getString("image_url")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateMenuItem(MenuItem item) {
        String sql = "UPDATE menu_items SET name = ?, price = ?, category = ?, image_url = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setDouble(2, item.getPrice());
            ps.setString(3, item.getCategory());
            ps.setString(4, item.getImageUrl());
            ps.setInt(5, item.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteMenuItem(int id) {
        String sql = "DELETE FROM menu_items WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
