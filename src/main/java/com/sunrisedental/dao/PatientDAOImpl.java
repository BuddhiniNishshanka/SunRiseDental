package com.sunrisedental.dao;

import com.sunrisedental.config.DatabaseConnection;
import com.sunrisedental.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PatientDAOImpl implements IPatientDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(PatientDAOImpl.class);

    @Override
    public Optional<Patient> findById(int id) {
        String sql = "SELECT id, patient_code, full_name, address, contact_no, email, dob, gender, created_at FROM patients WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding patient by id {}: {}", id, e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Patient> findByCode(String patientCode) {
        String sql = "SELECT id, patient_code, full_name, address, contact_no, email, dob, gender, created_at FROM patients WHERE patient_code = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding patient by code {}: {}", patientCode, e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Patient> findByContactNo(String contactNo) {
        String sql = "SELECT id, patient_code, full_name, address, contact_no, email, dob, gender, created_at FROM patients WHERE contact_no = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, contactNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding patient by contact no {}: {}", contactNo, e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Patient> searchPatients(String keyword) {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT id, patient_code, full_name, address, contact_no, email, dob, gender, created_at " +
                     "FROM patients WHERE full_name LIKE ? OR patient_code LIKE ? OR contact_no LIKE ? ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error searching patients: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Patient> findAll() {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT id, patient_code, full_name, address, contact_no, email, dob, gender, created_at FROM patients ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToPatient(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding all patients: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean createPatient(Patient patient) {
        String sql = "INSERT INTO patients (patient_code, full_name, address, contact_no, email, dob, gender) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, patient.getPatientCode());
            ps.setString(2, patient.getFullName());
            ps.setString(3, patient.getAddress());
            ps.setString(4, patient.getContactNo());
            ps.setString(5, patient.getEmail());
            ps.setDate(6, patient.getDob());
            ps.setString(7, patient.getGender());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        patient.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LOGGER.error("Error creating patient {}: {}", patient.getFullName(), e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean updatePatient(Patient patient) {
        String sql = "UPDATE patients SET full_name = ?, address = ?, contact_no = ?, email = ?, dob = ?, gender = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patient.getFullName());
            ps.setString(2, patient.getAddress());
            ps.setString(3, patient.getContactNo());
            ps.setString(4, patient.getEmail());
            ps.setDate(5, patient.getDob());
            ps.setString(6, patient.getGender());
            ps.setInt(7, patient.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Error updating patient {}: {}", patient.getId(), e.getMessage(), e);
        }
        return false;
    }

    @Override
    public int countTotalPatients() {
        String sql = "SELECT COUNT(*) FROM patients";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.error("Error counting patients: {}", e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public String generateNextPatientCode() {
        String sql = "SELECT MAX(id) FROM patients";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int nextId = rs.getInt(1) + 1;
                return String.format("PT-%04d", nextId);
            }
        } catch (SQLException e) {
            LOGGER.error("Error generating patient code: {}", e.getMessage(), e);
        }
        return "PT-1001";
    }

    private Patient mapResultSetToPatient(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setId(rs.getInt("id"));
        p.setPatientCode(rs.getString("patient_code"));
        p.setFullName(rs.getString("full_name"));
        p.setAddress(rs.getString("address"));
        p.setContactNo(rs.getString("contact_no"));
        p.setEmail(rs.getString("email"));
        p.setDob(rs.getDate("dob"));
        p.setGender(rs.getString("gender"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        return p;
    }
}
