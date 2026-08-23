package com.sunrisedental.dao;

import com.sunrisedental.config.DatabaseConnection;
import com.sunrisedental.model.Bill;
import com.sunrisedental.model.DoctorWorkloadDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BillingDAOImpl implements IBillingDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(BillingDAOImpl.class);

    private static final String BASE_SELECT_SQL =
            "SELECT b.id, b.bill_no, b.appointment_id, b.consultation_fee, b.treatment_cost, " +
            "b.discount_rate, b.discount_amount, b.tax_rate, b.tax_amount, b.total_amount, " +
            "b.payment_status, b.payment_method, b.bill_date, " +
            "a.appointment_no, a.appointment_date, a.appointment_time, " +
            "p.patient_code, p.full_name AS patient_name, p.contact_no AS patient_contact, " +
            "d.name AS dentist_name, t.treatment_code, t.name AS treatment_name " +
            "FROM bills b " +
            "JOIN appointments a ON b.appointment_id = a.id " +
            "JOIN patients p ON a.patient_id = p.id " +
            "JOIN dentists d ON a.dentist_id = d.id " +
            "JOIN treatments t ON a.treatment_id = t.id ";

    @Override
    public boolean createOrUpdateBill(Bill bill) {
        String sql = "INSERT INTO bills (bill_no, appointment_id, consultation_fee, treatment_cost, " +
                     "discount_rate, discount_amount, tax_rate, tax_amount, total_amount, payment_status, payment_method, bill_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW()) " +
                     "ON DUPLICATE KEY UPDATE consultation_fee = VALUES(consultation_fee), treatment_cost = VALUES(treatment_cost), " +
                     "discount_rate = VALUES(discount_rate), discount_amount = VALUES(discount_amount), " +
                     "tax_rate = VALUES(tax_rate), tax_amount = VALUES(tax_amount), total_amount = VALUES(total_amount), " +
                     "payment_status = VALUES(payment_status), payment_method = VALUES(payment_method), bill_date = NOW()";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, bill.getBillNo());
            ps.setInt(2, bill.getAppointmentId());
            ps.setBigDecimal(3, bill.getConsultationFee());
            ps.setBigDecimal(4, bill.getTreatmentCost());
            ps.setBigDecimal(5, bill.getDiscountRate());
            ps.setBigDecimal(6, bill.getDiscountAmount());
            ps.setBigDecimal(7, bill.getTaxRate());
            ps.setBigDecimal(8, bill.getTaxAmount());
            ps.setBigDecimal(9, bill.getTotalAmount());
            ps.setString(10, bill.getPaymentStatus() != null ? bill.getPaymentStatus() : "PAID");
            ps.setString(11, bill.getPaymentMethod() != null ? bill.getPaymentMethod() : "CASH");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                // Update appointment status to COMPLETED
                String updateAppSql = "UPDATE appointments SET status = 'COMPLETED' WHERE id = ?";
                try (PreparedStatement psApp = conn.prepareStatement(updateAppSql)) {
                    psApp.setInt(1, bill.getAppointmentId());
                    psApp.executeUpdate();
                }
                return true;
            }
        } catch (SQLException e) {
            LOGGER.error("Error creating/updating bill for appointment {}: {}", bill.getAppointmentId(), e.getMessage(), e);
        }
        return false;
    }

    @Override
    public Optional<Bill> generateInvoiceViaStoredProcedure(int appointmentId, BigDecimal discountRate, BigDecimal taxRate, String paymentMethod) {
        String callSql = "{CALL sp_GeneratePatientInvoice(?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             CallableStatement cs = conn.prepareCall(callSql)) {
            cs.setInt(1, appointmentId);
            cs.setBigDecimal(2, discountRate != null ? discountRate : BigDecimal.ZERO);
            cs.setBigDecimal(3, taxRate != null ? taxRate : BigDecimal.ZERO);
            cs.setString(4, paymentMethod != null ? paymentMethod : "CASH");
            cs.registerOutParameter(5, Types.INTEGER);
            cs.registerOutParameter(6, Types.DECIMAL);

            cs.execute();
            int billId = cs.getInt(5);
            if (billId > 0) {
                return findById(billId);
            }
        } catch (SQLException e) {
            LOGGER.error("Error invoking stored procedure sp_GeneratePatientInvoice for appointment {}: {}", appointmentId, e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Bill> findById(int id) {
        String sql = BASE_SELECT_SQL + "WHERE b.id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBill(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding bill by id {}: {}", id, e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Bill> findByAppointmentId(int appointmentId) {
        String sql = BASE_SELECT_SQL + "WHERE b.appointment_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBill(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding bill by appointmentId {}: {}", appointmentId, e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Bill> findByBillNo(String billNo) {
        String sql = BASE_SELECT_SQL + "WHERE b.bill_no = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, billNo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBill(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding bill by billNo {}: {}", billNo, e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Bill> findAll() {
        List<Bill> list = new ArrayList<>();
        String sql = BASE_SELECT_SQL + "ORDER BY b.bill_date DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToBill(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding all bills: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public BigDecimal calculateTotalRevenue(Date startDate, Date endDate) {
        String sql = "SELECT fn_CalculateTotalRevenue(?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, startDate);
            ps.setDate(2, endDate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal(1);
                    return total != null ? total : BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error calculating total revenue with function fn_CalculateTotalRevenue: {}", e.getMessage(), e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateTodayRevenue() {
        String sql = "SELECT IFNULL(SUM(total_amount), 0.00) FROM bills WHERE DATE(bill_date) = CURDATE() AND payment_status = 'PAID'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            LOGGER.error("Error calculating today's revenue: {}", e.getMessage(), e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateMonthlyRevenue() {
        String sql = "SELECT IFNULL(SUM(total_amount), 0.00) FROM bills WHERE MONTH(bill_date) = MONTH(CURDATE()) AND YEAR(bill_date) = YEAR(CURDATE()) AND payment_status = 'PAID'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            LOGGER.error("Error calculating monthly revenue: {}", e.getMessage(), e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public List<DoctorWorkloadDTO> getDoctorWorkloadReport() {
        List<DoctorWorkloadDTO> report = new ArrayList<>();
        String callSql = "{CALL sp_GetDoctorWorkloadReport()}";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             CallableStatement cs = conn.prepareCall(callSql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                DoctorWorkloadDTO dto = new DoctorWorkloadDTO(
                        rs.getInt("dentist_id"),
                        rs.getString("dentist_name"),
                        rs.getString("specialization"),
                        rs.getInt("total_appointments"),
                        rs.getInt("completed_appointments"),
                        rs.getInt("pending_appointments"),
                        rs.getBigDecimal("total_revenue_generated")
                );
                report.add(dto);
            }
        } catch (SQLException e) {
            LOGGER.error("Error generating doctor workload report: {}", e.getMessage(), e);
        }
        return report;
    }

    private Bill mapResultSetToBill(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setId(rs.getInt("id"));
        b.setBillNo(rs.getString("bill_no"));
        b.setAppointmentId(rs.getInt("appointment_id"));
        b.setConsultationFee(rs.getBigDecimal("consultation_fee"));
        b.setTreatmentCost(rs.getBigDecimal("treatment_cost"));
        b.setDiscountRate(rs.getBigDecimal("discount_rate"));
        b.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        b.setTaxRate(rs.getBigDecimal("tax_rate"));
        b.setTaxAmount(rs.getBigDecimal("tax_amount"));
        b.setTotalAmount(rs.getBigDecimal("total_amount"));
        b.setPaymentStatus(rs.getString("payment_status"));
        b.setPaymentMethod(rs.getString("payment_method"));
        b.setBillDate(rs.getTimestamp("bill_date"));

        b.setAppointmentNo(rs.getString("appointment_no"));
        b.setPatientName(rs.getString("patient_name"));
        b.setPatientCode(rs.getString("patient_code"));
        b.setPatientContact(rs.getString("patient_contact"));
        b.setDentistName(rs.getString("dentist_name"));
        b.setTreatmentName(rs.getString("treatment_name"));
        b.setTreatmentCode(rs.getString("treatment_code"));
        b.setAppointmentDate(rs.getString("appointment_date"));
        b.setAppointmentTime(rs.getString("appointment_time"));

        return b;
    }
}
