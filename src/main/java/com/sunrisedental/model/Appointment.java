package com.sunrisedental.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class Appointment {
    private int id;
    private String appointmentNo;
    private int patientId;
    private int dentistId;
    private int treatmentId;
    private Date appointmentDate;
    private Time appointmentTime;
    private String status; // 'SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'
    private String notes;
    private Timestamp createdAt;

    // Joined / Presentation fields
    private String patientName;
    private String patientCode;
    private String contactNo;
    private String address;
    private String dentistName;
    private String dentistSpecialization;
    private String dentistRoomNo;
    private BigDecimal dentistConsultationFee;
    private String treatmentName;
    private String treatmentCode;
    private BigDecimal treatmentStandardFee;

    // Billing status if exists
    private Integer billId;
    private String billNo;
    private BigDecimal totalBillAmount;
    private String paymentStatus;

    public Appointment() {}

    public Appointment(int id, String appointmentNo, int patientId, int dentistId, int treatmentId, Date appointmentDate, Time appointmentTime, String status, String notes) {
        this.id = id;
        this.appointmentNo = appointmentNo;
        this.patientId = patientId;
        this.dentistId = dentistId;
        this.treatmentId = treatmentId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
        this.notes = notes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAppointmentNo() { return appointmentNo; }
    public void setAppointmentNo(String appointmentNo) { this.appointmentNo = appointmentNo; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getDentistId() { return dentistId; }
    public void setDentistId(int dentistId) { this.dentistId = dentistId; }

    public int getTreatmentId() { return treatmentId; }
    public void setTreatmentId(int treatmentId) { this.treatmentId = treatmentId; }

    public Date getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(Date appointmentDate) { this.appointmentDate = appointmentDate; }

    public Time getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(Time appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientCode() { return patientCode; }
    public void setPatientCode(String patientCode) { this.patientCode = patientCode; }

    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDentistName() { return dentistName; }
    public void setDentistName(String dentistName) { this.dentistName = dentistName; }

    public String getDentistSpecialization() { return dentistSpecialization; }
    public void setDentistSpecialization(String dentistSpecialization) { this.dentistSpecialization = dentistSpecialization; }

    public String getDentistRoomNo() { return dentistRoomNo; }
    public void setDentistRoomNo(String dentistRoomNo) { this.dentistRoomNo = dentistRoomNo; }

    public BigDecimal getDentistConsultationFee() { return dentistConsultationFee; }
    public void setDentistConsultationFee(BigDecimal dentistConsultationFee) { this.dentistConsultationFee = dentistConsultationFee; }

    public String getTreatmentName() { return treatmentName; }
    public void setTreatmentName(String treatmentName) { this.treatmentName = treatmentName; }

    public String getTreatmentCode() { return treatmentCode; }
    public void setTreatmentCode(String treatmentCode) { this.treatmentCode = treatmentCode; }

    public BigDecimal getTreatmentStandardFee() { return treatmentStandardFee; }
    public void setTreatmentStandardFee(BigDecimal treatmentStandardFee) { this.treatmentStandardFee = treatmentStandardFee; }

    public Integer getBillId() { return billId; }
    public void setBillId(Integer billId) { this.billId = billId; }

    public String getBillNo() { return billNo; }
    public void setBillNo(String billNo) { this.billNo = billNo; }

    public BigDecimal getTotalBillAmount() { return totalBillAmount; }
    public void setTotalBillAmount(BigDecimal totalBillAmount) { this.totalBillAmount = totalBillAmount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}
