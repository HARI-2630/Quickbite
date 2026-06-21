package com.quickbite.dao;

import com.quickbite.connection.DBConnection;
import com.quickbite.models.OtpSession;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class OtpDAO {
      
    public OtpSession getSession(String phoneOrEmail) {
        String sql = "SELECT * FROM otp_sessions WHERE phone_or_email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phoneOrEmail);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new OtpSession(
                        rs.getInt("id"),
                        rs.getString("phone_or_email"),
                        rs.getString("otp_code"),
                        rs.getInt("resend_count"),
                        rs.getTimestamp("last_requested_at"),
                        rs.getTimestamp("expires_at"),
                        rs.getInt("verified") == 1
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean saveSession(OtpSession session) {
        OtpSession existing = getSession(session.getPhoneOrEmail());
        if (existing == null) {
            String sql = "INSERT INTO otp_sessions (phone_or_email, otp_code, resend_count, last_requested_at, expires_at, verified) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, session.getPhoneOrEmail());
                ps.setString(2, session.getOtpCode());
                ps.setInt(3, session.getResendCount());
                ps.setTimestamp(4, session.getLastRequestedAt());
                ps.setTimestamp(5, session.getExpiresAt());
                ps.setInt(6, session.isVerified() ? 1 : 0);
                return ps.executeUpdate() > 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String sql = "UPDATE otp_sessions SET otp_code = ?, resend_count = ?, last_requested_at = ?, expires_at = ?, verified = ? WHERE phone_or_email = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, session.getOtpCode());
                ps.setInt(2, session.getResendCount());
                ps.setTimestamp(3, session.getLastRequestedAt());
                ps.setTimestamp(4, session.getExpiresAt());
                ps.setInt(5, session.isVerified() ? 1 : 0);
                ps.setString(6, session.getPhoneOrEmail());
                return ps.executeUpdate() > 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean deleteSession(String phoneOrEmail) {
        String sql = "DELETE FROM otp_sessions WHERE phone_or_email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phoneOrEmail);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
