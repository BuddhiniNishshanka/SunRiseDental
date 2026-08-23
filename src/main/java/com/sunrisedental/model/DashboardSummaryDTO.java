package com.sunrisedental.model;

import java.math.BigDecimal;
import java.util.List;

public class DashboardSummaryDTO {
    private int totalPatients;
    private int totalAppointments;
    private int todayAppointments;
    private int completedAppointments;
    private int pendingAppointments;
    private BigDecimal todayRevenue;
    private BigDecimal monthlyRevenue;
    private List<Appointment> recentAppointments;

    public DashboardSummaryDTO() {}

    public int getTotalPatients() { return totalPatients; }
    public void setTotalPatients(int totalPatients) { this.totalPatients = totalPatients; }

    public int getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(int totalAppointments) { this.totalAppointments = totalAppointments; }

    public int getTodayAppointments() { return todayAppointments; }
    public void setTodayAppointments(int todayAppointments) { this.todayAppointments = todayAppointments; }

    public int getCompletedAppointments() { return completedAppointments; }
    public void setCompletedAppointments(int completedAppointments) { this.completedAppointments = completedAppointments; }

    public int getPendingAppointments() { return pendingAppointments; }
    public void setPendingAppointments(int pendingAppointments) { this.pendingAppointments = pendingAppointments; }

    public BigDecimal getTodayRevenue() { return todayRevenue; }
    public void setTodayRevenue(BigDecimal todayRevenue) { this.todayRevenue = todayRevenue; }

    public BigDecimal getMonthlyRevenue() { return monthlyRevenue; }
    public void setMonthlyRevenue(BigDecimal monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }

    public List<Appointment> getRecentAppointments() { return recentAppointments; }
    public void setRecentAppointments(List<Appointment> recentAppointments) { this.recentAppointments = recentAppointments; }
}
