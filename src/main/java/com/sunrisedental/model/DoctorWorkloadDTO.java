package com.sunrisedental.model;

import java.math.BigDecimal;

public class DoctorWorkloadDTO {
    private int dentistId;
    private String dentistName;
    private String specialization;
    private int totalAppointments;
    private int completedAppointments;
    private int pendingAppointments;
    private BigDecimal totalRevenueGenerated;

    public DoctorWorkloadDTO() {}

    public DoctorWorkloadDTO(int dentistId, String dentistName, String specialization, int totalAppointments, int completedAppointments, int pendingAppointments, BigDecimal totalRevenueGenerated) {
        this.dentistId = dentistId;
        this.dentistName = dentistName;
        this.specialization = specialization;
        this.totalAppointments = totalAppointments;
        this.completedAppointments = completedAppointments;
        this.pendingAppointments = pendingAppointments;
        this.totalRevenueGenerated = totalRevenueGenerated;
    }

    public int getDentistId() { return dentistId; }
    public void setDentistId(int dentistId) { this.dentistId = dentistId; }

    public String getDentistName() { return dentistName; }
    public void setDentistName(String dentistName) { this.dentistName = dentistName; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public int getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(int totalAppointments) { this.totalAppointments = totalAppointments; }

    public int getCompletedAppointments() { return completedAppointments; }
    public void setCompletedAppointments(int completedAppointments) { this.completedAppointments = completedAppointments; }

    public int getPendingAppointments() { return pendingAppointments; }
    public void setPendingAppointments(int pendingAppointments) { this.pendingAppointments = pendingAppointments; }

    public BigDecimal getTotalRevenueGenerated() { return totalRevenueGenerated; }
    public void setTotalRevenueGenerated(BigDecimal totalRevenueGenerated) { this.totalRevenueGenerated = totalRevenueGenerated; }
}
