package com.sunrisedental.dao;

import com.sunrisedental.config.DatabaseConnection;
import com.sunrisedental.model.Treatment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TreatmentDAOImpl implements ITreatmentDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(TreatmentDAOImpl.class);

    @Override
    public Optional<Treatment> findById(int id) {
        String sql = "SELECT id, treatment_code, name, description, standard_fee, category FROM treatments WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTreatment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding treatment by id {}: {}", id, e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Treatment> findByCode(String code) {
        String sql = "SELECT id, treatment_code, name, description, standard_fee, category FROM treatments WHERE treatment_code = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTreatment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding treatment by code {}: {}", code, e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Treatment> findAll() {
        List<Treatment> list = new ArrayList<>();
        String sql = "SELECT id, treatment_code, name, description, standard_fee, category FROM treatments ORDER BY category, name ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToTreatment(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding all treatments: {}", e.getMessage(), e);
        }
        return list;
    }

    private Treatment mapResultSetToTreatment(ResultSet rs) throws SQLException {
        Treatment t = new Treatment();
        t.setId(rs.getInt("id"));
        t.setTreatmentCode(rs.getString("treatment_code"));
        t.setName(rs.getString("name"));
        t.setDescription(rs.getString("description"));
        t.setStandardFee(rs.getBigDecimal("standard_fee"));
        t.setCategory(rs.getString("category"));
        return t;
    }
}
