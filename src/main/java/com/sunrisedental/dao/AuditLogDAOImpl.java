package com.sunrisedental.dao;

import com.sunrisedental.config.DatabaseConnection;
import com.sunrisedental.model.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAOImpl implements IAuditLogDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogDAOImpl.class);

    @Override
    public boolean logAction(AuditLog log) {
        String sql = "INSERT INTO audit_logs (username, action, details, ip_address) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, log.getUsername());
            ps.setString(2, log.getAction());
            ps.setString(3, log.getDetails());
            ps.setString(4, log.getIpAddress() != null ? log.getIpAddress() : "127.0.0.1");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Error writing audit log: {}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public List<AuditLog> findRecentLogs(int limit) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT id, username, action, details, ip_address, created_at FROM audit_logs ORDER BY id DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = new AuditLog();
                    log.setId(rs.getInt("id"));
                    log.setUsername(rs.getString("username"));
                    log.setAction(rs.getString("action"));
                    log.setDetails(rs.getString("details"));
                    log.setIpAddress(rs.getString("ip_address"));
                    log.setTimestamp(rs.getTimestamp("created_at"));
                    list.add(log);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error retrieving audit logs: {}", e.getMessage(), e);
        }
        return list;
    }
}
