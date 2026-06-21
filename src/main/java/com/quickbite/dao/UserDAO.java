package com.quickbite.dao;

import com.quickbite.connection.DBConnection;
import com.quickbite.models.User;
import com.quickbite.security.PasswordHash;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (name, email, password, role, phone, google_id, avatar_url, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            
            // If the password is not already PBKDF2 hashed, hash it!
            String rawPassword = user.getPassword();
            String passwordToStore = (rawPassword != null && rawPassword.contains(":")) ? rawPassword : PasswordHash.hash(rawPassword);
            ps.setString(3, passwordToStore != null ? passwordToStore : "");
            ps.setString(4, user.getRole());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getGoogleId());
            ps.setString(7, user.getAvatarUrl());
            ps.setString(8, user.getStatus() != null ? user.getStatus() : "ACTIVE");
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public User validateLogin(String email, String password) {
        User user = getUserByEmail(email);
        if (user != null) {
            if (PasswordHash.verify(password, user.getPassword())) {
                if ("BLOCKED".equalsIgnoreCase(user.getStatus())) {
                    return null; // Blocked user cannot login
                }
                return user;
            }
        }
        return null;
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserByPhone(String phone) {
        String sql = "SELECT * FROM users WHERE phone = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserByGoogleId(String googleId) {
        String sql = "SELECT * FROM users WHERE google_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, googleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapUser(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateUserProfile(User user) {
        String sql = "UPDATE users SET name = ?, email = ?, phone = ?, avatar_url = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getAvatarUrl());
            ps.setInt(5, user.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateUserPassword(int id, String hashedPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateUserRole(int id, String role) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateUserStatus(int id, String status) {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
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

    private User mapUser(ResultSet rs) throws Exception {
        return new User(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("role"),
            rs.getString("phone"),
            rs.getString("google_id"),
            rs.getString("avatar_url"),
            rs.getString("status"),
            rs.getTimestamp("created_at")
        );
    }
}
