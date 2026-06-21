package com.quickbite.dao;

import com.quickbite.connection.DBConnection;
import com.quickbite.models.AuditLog;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    public boolean insertLog(AuditLog log) {
        String sql = "INSERT INTO audit_logs (user_id, action, ip_address, user_agent) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (log.getUserId() != null) {
                ps.setInt(1, log.getUserId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, log.getAction());
            ps.setString(3, log.getIpAddress());
            ps.setString(4, log.getUserAgent());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<AuditLog> getLogsByUserId(int userId) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs WHERE user_id = ? ORDER BY created_at DESC LIMIT 50";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AuditLog(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("action"),
                        rs.getString("ip_address"),
                        rs.getString("user_agent"),
                        rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<AuditLog> getAllLogs() {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY created_at DESC LIMIT 100";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new AuditLog(
                    rs.getInt("id"),
                    rs.getObject("user_id") != null ? rs.getInt("user_id") : null,
                    rs.getString("action"),
                    rs.getString("ip_address"),
                    rs.getString("user_agent"),
                    rs.getTimestamp("created_at")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
