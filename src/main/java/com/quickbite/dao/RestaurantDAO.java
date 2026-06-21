package com.quickbite.dao;

import com.quickbite.connection.DBConnection;
import com.quickbite.models.Restaurant;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RestaurantDAO {

    public boolean addRestaurant(Restaurant restaurant) {
        String sql = "INSERT INTO restaurants (name, cuisine, owner_id, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, restaurant.getName());
            ps.setString(2, restaurant.getCuisine());
            ps.setInt(3, restaurant.getOwnerId());
            ps.setString(4, restaurant.getStatus());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Restaurant> getAllRestaurants() {
        List<Restaurant> list = new ArrayList<>();
        String sql = "SELECT * FROM restaurants";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Restaurant(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("cuisine"),
                    rs.getInt("owner_id"),
                    rs.getString("status")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Restaurant> getRestaurantsByOwner(int ownerId) {
        List<Restaurant> list = new ArrayList<>();
        String sql = "SELECT * FROM restaurants WHERE owner_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Restaurant(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("cuisine"),
                        rs.getInt("owner_id"),
                        rs.getString("status")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Restaurant getRestaurantById(int id) {
        String sql = "SELECT * FROM restaurants WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Restaurant(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("cuisine"),
                        rs.getInt("owner_id"),
                        rs.getString("status")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateRestaurantStatus(int id, String status) {
        String sql = "UPDATE restaurants SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteRestaurant(int id) {
        String sql = "DELETE FROM restaurants WHERE id = ?";
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
