package com.sunrisedental.dao;

import com.sunrisedental.config.DatabaseConnection;
import com.sunrisedental.model.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AppointmentDAOImpl implements IAppointmentDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentDAOImpl.class);

    private static final String BASE_SELECT_SQL =
            "SELECT a.id, a.appointment_no, a.patient_id, a.dentist_id, a.treatment_id, " +
            "a.appointment_date, a.appointment_time, a.status, a.notes, a.created_at, " +
            "p.patient_code, p.full_name AS patient_name, p.contact_no, p.address, " +
            "d.name AS dentist_name, d.specialization AS dentist_specialization, d.room_no AS dentist_room_no, d.consultation_fee, " +
            "t.treatment_code, t.name AS treatment_name, t.standard_fee AS treatment_fee, " +
            "b.id AS bill_id, b.bill_no, b.total_amount AS total_bill_amount, b.payment_status " +
            "FROM appointments a " +
            "JOIN patients p ON a.patient_id = p.id " +
            "JOIN dentists d ON a.dentist_id = d.id " +
            "JOIN treatments t ON a.treatment_id = t.id " +
            "LEFT JOIN bills b ON a.id = b.appointment_id ";

    @Override
    public boolean createAppointment(Appointment appointment) {
        String sql = "INSERT INTO appointments (appointment_no, patient_id, dentist_id, treatment_id, appointment_date, appointment_time, status, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, appointment.getAppointmentNo());
            ps.setInt(2, appointment.getPatientId());
            ps.setInt(3, appointment.getDentistId());
            ps.setInt(4, appointment.getTreatmentId());
            ps.setDate(5, appointment.getAppointmentDate());
            ps.setTime(6, appointment.getAppointmentTime());
            ps.setString(7, appointment.getStatus() != null ? appointment.getStatus() : "SCHEDULED");
            ps.setString(8, appointment.getNotes());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        appointment.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LOGGER.error("Error creating appointment {}: {}", appointment.getAppointmentNo(), e.getMessage(), e);
        }
        return false;
    }

    @Override
    public Optional<Appointment> findById(int id) {
        String sql = BASE_SELECT_SQL + "WHERE a.id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding appointment by id {}: {}", id, e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Appointment> findByAppointmentNo(String appointmentNo) {
        String sql = BASE_SELECT_SQL + "WHERE a.appointment_no = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointmentNo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding appointment by no {}: {}", appointmentNo, e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Appointment> searchAppointments(String query) {
        List<Appointment> list = new ArrayList<>();
        String sql = BASE_SELECT_SQL +
                     "WHERE a.appointment_no LIKE ? OR p.full_name LIKE ? OR p.contact_no LIKE ? OR d.name LIKE ? " +
                     "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + (query != null ? query.trim() : "") + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error searching appointments with query {}: {}", query, e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Appointment> findAll() {
        List<Appointment> list = new ArrayList<>();
        String sql = BASE_SELECT_SQL + "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToAppointment(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding all appointments: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Appointment> findTodayAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = BASE_SELECT_SQL + "WHERE a.appointment_date = CURDATE() ORDER BY a.appointment_time ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToAppointment(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding today's appointments: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Appointment> findRecentAppointments(int limit) {
        List<Appointment> list = new ArrayList<>();
        String sql = BASE_SELECT_SQL + "ORDER BY a.id DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding recent appointments: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean hasConflict(int dentistId, Date date, Time time, Integer excludeAppointmentId) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM appointments " +
                "WHERE dentist_id = ? AND appointment_date = ? AND appointment_time = ? AND status != 'CANCELLED'");
        if (excludeAppointmentId != null && excludeAppointmentId > 0) {
            sql.append(" AND id != ?");
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, dentistId);
            ps.setDate(2, date);
            ps.setTime(3, time);
            if (excludeAppointmentId != null && excludeAppointmentId > 0) {
                ps.setInt(4, excludeAppointmentId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error checking appointment conflict: {}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Error updating appointment status: {}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public String generateNextAppointmentNo() {
        String sql = "SELECT MAX(id) FROM appointments";
        int year = LocalDate.now().getYear();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int nextId = rs.getInt(1) + 1;
                return String.format("APP-%d-%03d", year, nextId);
            }
        } catch (SQLException e) {
            LOGGER.error("Error generating next appointment number: {}", e.getMessage(), e);
        }
        return String.format("APP-%d-001", year);
    }

    @Override
    public int countTotalAppointments() {
        String sql = "SELECT COUNT(*) FROM appointments";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.error("Error counting total appointments: {}", e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public int countTodayAppointments() {
        String sql = "SELECT COUNT(*) FROM appointments WHERE appointment_date = CURDATE()";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.error("Error counting today's appointments: {}", e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE status = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error counting appointments by status {}: {}", status, e.getMessage(), e);
        }
        return 0;
    }

    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setId(rs.getInt("id"));
        a.setAppointmentNo(rs.getString("appointment_no"));
        a.setPatientId(rs.getInt("patient_id"));
        a.setDentistId(rs.getInt("dentist_id"));
        a.setTreatmentId(rs.getInt("treatment_id"));
        a.setAppointmentDate(rs.getDate("appointment_date"));
        a.setAppointmentTime(rs.getTime("appointment_time"));
        a.setStatus(rs.getString("status"));
        a.setNotes(rs.getString("notes"));
        a.setCreatedAt(rs.getTimestamp("created_at"));

        a.setPatientCode(rs.getString("patient_code"));
        a.setPatientName(rs.getString("patient_name"));
        a.setContactNo(rs.getString("contact_no"));
        a.setAddress(rs.getString("address"));

        a.setDentistName(rs.getString("dentist_name"));
        a.setDentistSpecialization(rs.getString("dentist_specialization"));
        a.setDentistRoomNo(rs.getString("dentist_room_no"));
        a.setDentistConsultationFee(rs.getBigDecimal("consultation_fee"));

        a.setTreatmentCode(rs.getString("treatment_code"));
        a.setTreatmentName(rs.getString("treatment_name"));
        a.setTreatmentStandardFee(rs.getBigDecimal("treatment_fee"));

        int billId = rs.getInt("bill_id");
        if (!rs.wasNull() && billId > 0) {
            a.setBillId(billId);
            a.setBillNo(rs.getString("bill_no"));
            a.setTotalBillAmount(rs.getBigDecimal("total_bill_amount"));
            a.setPaymentStatus(rs.getString("payment_status"));
        }
        return a;
    }
}
