package com.sunrisedental.dao;

import com.sunrisedental.model.Bill;
import com.sunrisedental.model.DoctorWorkloadDTO;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

public interface IBillingDAO {
    boolean createOrUpdateBill(Bill bill);
    Optional<Bill> generateInvoiceViaStoredProcedure(int appointmentId, BigDecimal discountRate, BigDecimal taxRate, String paymentMethod);
    Optional<Bill> findById(int id);
    Optional<Bill> findByAppointmentId(int appointmentId);
    Optional<Bill> findByBillNo(String billNo);
    List<Bill> findAll();
    BigDecimal calculateTotalRevenue(Date startDate, Date endDate);
    BigDecimal calculateTodayRevenue();
    BigDecimal calculateMonthlyRevenue();
    List<DoctorWorkloadDTO> getDoctorWorkloadReport();
}
