package com.sunrisedental;

import com.sunrisedental.dao.*;
import com.sunrisedental.model.*;
import com.sunrisedental.service.BillingService;
import com.sunrisedental.service.strategy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("Task C: Billing Engine & Strategy Pattern Unit Tests")
public class BillingServiceTest {

    private IBillingDAO mockBillingDAO;
    private IAppointmentDAO mockAppointmentDAO;
    private IPatientDAO mockPatientDAO;
    private ITreatmentDAO mockTreatmentDAO;
    private IAuditLogDAO mockAuditLogDAO;
    private BillingService billingService;

    @BeforeEach
    void setUp() {
        mockBillingDAO = Mockito.mock(IBillingDAO.class);
        mockAppointmentDAO = Mockito.mock(IAppointmentDAO.class);
        mockPatientDAO = Mockito.mock(IPatientDAO.class);
        mockTreatmentDAO = Mockito.mock(ITreatmentDAO.class);
        mockAuditLogDAO = Mockito.mock(IAuditLogDAO.class);

        billingService = new BillingService(
                mockBillingDAO, mockAppointmentDAO, mockPatientDAO, mockTreatmentDAO, mockAuditLogDAO
        );
    }

    @Test
    @DisplayName("TC-BILL-01: Standard Billing Calculation without discounts/taxes")
    void testStandardBillingCalculation() {
        StandardBillingStrategy strategy = new StandardBillingStrategy();
        BigDecimal consultation = new BigDecimal("2500.00");
        BigDecimal treatment = new BigDecimal("4500.00");

        BillingCalculationResult result = strategy.calculate(consultation, treatment, BigDecimal.ZERO, BigDecimal.ZERO);

        assertEquals(new BigDecimal("7000.00"), result.getSubtotal(), "Subtotal must equal consultation + treatment");
        assertEquals(new BigDecimal("0.00"), result.getDiscountAmount());
        assertEquals(new BigDecimal("0.00"), result.getTaxAmount());
        assertEquals(new BigDecimal("7000.00"), result.getTotalAmount(), "Total must equal subtotal when discount is 0");
    }

    @Test
    @DisplayName("TC-BILL-02: Standard Billing with 10% Discount and 5% Tax")
    void testBillingWithDiscountAndTax() {
        StandardBillingStrategy strategy = new StandardBillingStrategy();
        BigDecimal consultation = new BigDecimal("3000.00");
        BigDecimal treatment = new BigDecimal("18000.00");
        BigDecimal discountRate = new BigDecimal("10.00");
        BigDecimal taxRate = new BigDecimal("5.00");

        BillingCalculationResult result = strategy.calculate(consultation, treatment, discountRate, taxRate);

        // Subtotal = 21,000.00
        // Discount 10% = 2,100.00 -> Taxable = 18,900.00
        // Tax 5% = 945.00
        // Total = 19,845.00
        assertEquals(new BigDecimal("21000.00"), result.getSubtotal());
        assertEquals(new BigDecimal("2100.00"), result.getDiscountAmount());
        assertEquals(new BigDecimal("945.00"), result.getTaxAmount());
        assertEquals(new BigDecimal("19845.00"), result.getTotalAmount());
    }

    @Test
    @DisplayName("TC-BILL-03: Senior Citizen Care Strategy (10% Automated Base Subsidy)")
    void testSeniorCitizenBillingStrategy() {
        SeniorDiscountBillingStrategy strategy = new SeniorDiscountBillingStrategy();
        BigDecimal consultation = new BigDecimal("2500.00");
        BigDecimal treatment = new BigDecimal("7500.00");
        BigDecimal additionalDiscount = new BigDecimal("5.00"); // 10% base + 5% promo = 15% total

        BillingCalculationResult result = strategy.calculate(consultation, treatment, additionalDiscount, BigDecimal.ZERO);

        // Subtotal = 10,000.00
        // Total Discount Rate = 15% -> Discount Amount = 1,500.00
        // Total = 8,500.00
        assertEquals(new BigDecimal("10000.00"), result.getSubtotal());
        assertEquals(new BigDecimal("15.00"), result.getDiscountRate());
        assertEquals(new BigDecimal("1500.00"), result.getDiscountAmount());
        assertEquals(new BigDecimal("8500.00"), result.getTotalAmount());
    }

    @Test
    @DisplayName("TC-BILL-04: Surgical Procedure Strategy with Sterilization Surcharge")
    void testSurgicalBillingStrategy() {
        SurgicalBillingStrategy strategy = new SurgicalBillingStrategy();
        BigDecimal consultation = new BigDecimal("3500.00");
        BigDecimal treatment = new BigDecimal("12500.00");

        BillingCalculationResult result = strategy.calculate(consultation, treatment, BigDecimal.ZERO, BigDecimal.ZERO);

        // Treatment becomes 12,500 + 1,500 = 14,000.00
        // Subtotal = 3,500 + 14,000 = 17,500.00
        assertEquals(new BigDecimal("14000.00"), result.getTreatmentCost());
        assertEquals(new BigDecimal("17500.00"), result.getSubtotal());
        assertEquals(new BigDecimal("17500.00"), result.getTotalAmount());
    }

    @Test
    @DisplayName("TC-BILL-05: 100% Full Discount Boundary Condition")
    void testFullDiscountBoundary() {
        StandardBillingStrategy strategy = new StandardBillingStrategy();
        BigDecimal consultation = new BigDecimal("3000.00");
        BigDecimal treatment = new BigDecimal("5000.00");

        BillingCalculationResult result = strategy.calculate(consultation, treatment, new BigDecimal("100.00"), BigDecimal.ZERO);

        assertEquals(new BigDecimal("8000.00"), result.getSubtotal());
        assertEquals(new BigDecimal("8000.00"), result.getDiscountAmount());
        assertEquals(new BigDecimal("0.00"), result.getTotalAmount(), "Total must be 0.00 under 100% discount");
    }

    @Test
    @DisplayName("TC-BILL-06: End-to-End Invoice Processing Integration")
    void testProcessPatientInvoice() {
        Appointment app = new Appointment(10, "APP-2026-010", 1, 1, 1, Date.valueOf("2026-08-25"), Time.valueOf("10:00:00"), "SCHEDULED", "Checkup");
        app.setDentistConsultationFee(new BigDecimal("2500.00"));
        app.setTreatmentStandardFee(new BigDecimal("3500.00"));

        Patient patient = new Patient(1, "PT-1001", "Kamal", "Colombo", "0771234567", "k@gmail.com", Date.valueOf("1990-01-01"), "MALE");
        Treatment trt = new Treatment(1, "TRT-001", "Checkup", "Desc", new BigDecimal("3500.00"), "GENERAL");

        when(mockAppointmentDAO.findById(10)).thenReturn(Optional.of(app));
        when(mockPatientDAO.findById(1)).thenReturn(Optional.of(patient));
        when(mockTreatmentDAO.findById(1)).thenReturn(Optional.of(trt));
        when(mockBillingDAO.createOrUpdateBill(any())).thenReturn(true);
        when(mockBillingDAO.findByAppointmentId(10)).thenReturn(Optional.of(new Bill()));

        StringBuilder error = new StringBuilder();
        Optional<Bill> bill = billingService.processPatientInvoice(10, BigDecimal.ZERO, BigDecimal.ZERO, "CASH", "reception", error);

        assertTrue(bill.isPresent(), "Invoice must be processed and persisted successfully");
        assertEquals(0, error.length());
    }
}
