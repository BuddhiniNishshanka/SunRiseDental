package com.sunrisedental.dao;

import com.sunrisedental.config.DatabaseConnection;
import com.sunrisedental.model.Dentist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DentistDAOImpl implements IDentistDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(DentistDAOImpl.class);

    @Override
    public Optional<Dentist> findById(int id) {
        String sql = "SELECT id, name, specialization, room_no, contact_no, email, consultation_fee, is_available FROM dentists WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDentist(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding dentist by id {}: {}", id, e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Dentist> findAllAvailable() {
        List<Dentist> list = new ArrayList<>();
        String sql = "SELECT id, name, specialization, room_no, contact_no, email, consultation_fee, is_available FROM dentists WHERE is_available = TRUE ORDER BY name ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToDentist(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Error listing available dentists: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Dentist> findAll() {
        List<Dentist> list = new ArrayList<>();
        String sql = "SELECT id, name, specialization, room_no, contact_no, email, consultation_fee, is_available FROM dentists ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToDentist(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Error listing all dentists: {}", e.getMessage(), e);
        }
        return list;
    }

    private Dentist mapResultSetToDentist(ResultSet rs) throws SQLException {
        Dentist d = new Dentist();
        d.setId(rs.getInt("id"));
        d.setName(rs.getString("name"));
        d.setSpecialization(rs.getString("specialization"));
        d.setRoomNo(rs.getString("room_no"));
        d.setContactNo(rs.getString("contact_no"));
        d.setEmail(rs.getString("email"));
        d.setConsultationFee(rs.getBigDecimal("consultation_fee"));
        d.setAvailable(rs.getBoolean("is_available"));
        return d;
    }
}
