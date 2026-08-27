package com.sunrisedental.service;

import com.sunrisedental.dao.*;
import com.sunrisedental.factory.BillingCalculatorFactory;
import com.sunrisedental.factory.DAOFactory;
import com.sunrisedental.model.*;
import com.sunrisedental.service.strategy.BillingCalculationResult;
import com.sunrisedental.service.strategy.IBillingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

public class BillingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BillingService.class);

    private final IBillingDAO billingDAO;
    private final IAppointmentDAO appointmentDAO;
    private final IPatientDAO patientDAO;
    private final ITreatmentDAO treatmentDAO;
    private final IAuditLogDAO auditLogDAO;

    public BillingService() {
        this(
                DAOFactory.getInstance().getBillingDAO(),
                DAOFactory.getInstance().getAppointmentDAO(),
                DAOFactory.getInstance().getPatientDAO(),
                DAOFactory.getInstance().getTreatmentDAO(),
                DAOFactory.getInstance().getAuditLogDAO()
        );
    }

    public BillingService(IBillingDAO billingDAO, IAppointmentDAO appointmentDAO,
                          IPatientDAO patientDAO, ITreatmentDAO treatmentDAO,
                          IAuditLogDAO auditLogDAO) {
        this.billingDAO = billingDAO;
        this.appointmentDAO = appointmentDAO;
        this.patientDAO = patientDAO;
        this.treatmentDAO = treatmentDAO;
        this.auditLogDAO = auditLogDAO;
    }

    /**
     * Preview bill calculation using Strategy Pattern without saving.
     */
    public BillingCalculationResult previewBillCalculation(int appointmentId, BigDecimal discountRate, BigDecimal taxRate, StringBuilder error) {
        Optional<Appointment> appOpt = appointmentDAO.findById(appointmentId);
        if (appOpt.isEmpty()) {
            error.append("Appointment not found. ");
            return null;
        }

        Appointment app = appOpt.get();
        boolean isSenior = false;
        Optional<Patient> patientOpt = patientDAO.findById(app.getPatientId());
        if (patientOpt.isPresent() && patientOpt.get().getDob() != null) {
            int age = Period.between(patientOpt.get().getDob().toLocalDate(), LocalDate.now()).getYears();
            if (age >= 60) {
                isSenior = true;
            }
        }

        String category = "GENERAL";
        Optional<Treatment> trtOpt = treatmentDAO.findById(app.getTreatmentId());
        if (trtOpt.isPresent()) {
            category = trtOpt.get().getCategory();
        }

        IBillingStrategy strategy = BillingCalculatorFactory.getStrategy(category, isSenior);
        return strategy.calculate(app.getDentistConsultationFee(), app.getTreatmentStandardFee(), discountRate, taxRate);
    }

    /**
     * Process and persist patient billing & invoice generation.
     */
    public Optional<Bill> processPatientInvoice(
            int appointmentId,
            BigDecimal discountRate,
            BigDecimal taxRate,
            String paymentMethod,
            String operatorUsername,
            StringBuilder errorMessage
    ) {
        Optional<Appointment> appOpt = appointmentDAO.findById(appointmentId);
        if (appOpt.isEmpty()) {
            errorMessage.append("Appointment not found. ");
            return Optional.empty();
        }

        Appointment app = appOpt.get();
        if ("CANCELLED".equalsIgnoreCase(app.getStatus())) {
            errorMessage.append("Cannot generate bill for a cancelled appointment. ");
            return Optional.empty();
        }

        // Preview calculation to get proper strategy results
        BillingCalculationResult calc = previewBillCalculation(appointmentId, discountRate, taxRate, errorMessage);
        if (calc == null) {
            return Optional.empty();
        }

        // Try invocation via MySQL Stored Procedure first for high enterprise standard
        Optional<Bill> spResult = billingDAO.generateInvoiceViaStoredProcedure(
                appointmentId, calc.getDiscountRate(), calc.getTaxRate(), paymentMethod
        );

        Bill finalBill;
        if (spResult.isPresent()) {
            finalBill = spResult.get();
        } else {
            // Fallback to transactional Java DAO update
            String billNo = String.format("INV-%d-%04d", LocalDate.now().getYear(), appointmentId);
            Bill newBill = new Bill();
            newBill.setBillNo(billNo);
            newBill.setAppointmentId(appointmentId);
            newBill.setConsultationFee(calc.getConsultationFee());
            newBill.setTreatmentCost(calc.getTreatmentCost());
            newBill.setDiscountRate(calc.getDiscountRate());
            newBill.setDiscountAmount(calc.getDiscountAmount());
            newBill.setTaxRate(calc.getTaxRate());
            newBill.setTaxAmount(calc.getTaxAmount());
            newBill.setTotalAmount(calc.getTotalAmount());
            newBill.setPaymentStatus("PAID");
            newBill.setPaymentMethod(paymentMethod != null ? paymentMethod : "CASH");

            boolean saved = billingDAO.createOrUpdateBill(newBill);
            if (!saved) {
                errorMessage.append("Failed to persist billing record.");
                return Optional.empty();
            }
            finalBill = billingDAO.findByAppointmentId(appointmentId).orElse(newBill);
        }

        // Observer Notification
        NotificationService.notifyInvoicePaid(finalBill);

        // Email Invoice to Patient
        try {
            Optional<Patient> patientOpt = patientDAO.findById(app.getPatientId());
            if (patientOpt.isPresent() && patientOpt.get().getEmail() != null) {
                String patientEmail = patientOpt.get().getEmail();
                boolean emailSent = EmailService.getInstance().sendInvoiceEmail(patientEmail, finalBill);
                LOGGER.info("Invoice email to patient {}: {}", patientEmail, emailSent ? "SENT" : "FAILED");
            } else {
                LOGGER.warn("Patient email not found for appointment {}. Skipping email.", appointmentId);
            }
        } catch (Exception emailEx) {
            LOGGER.error("Email sending failed but invoice was saved: {}", emailEx.getMessage());
        }

        // Security Audit Log
        auditLogDAO.logAction(new AuditLog(
                operatorUsername != null ? operatorUsername : "system",
                "INVOICE_GENERATED",
                String.format("Generated invoice %s for Appointment %s. Total: LKR %.2f",
                        finalBill.getBillNo(), app.getAppointmentNo(), finalBill.getTotalAmount()),
                "127.0.0.1"
        ));

        return Optional.of(finalBill);
    }

    public Optional<Bill> getBillById(int id) {
        return billingDAO.findById(id);
    }

    public Optional<Bill> getBillByAppointmentId(int appointmentId) {
        return billingDAO.findByAppointmentId(appointmentId);
    }

    public Optional<Bill> getBillByBillNo(String billNo) {
        return billingDAO.findByBillNo(billNo);
    }

    public List<Bill> getAllBills() {
        return billingDAO.findAll();
    }
}
