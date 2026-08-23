package com.sunrisedental.service;

import com.sunrisedental.dao.IAppointmentDAO;
import com.sunrisedental.dao.IBillingDAO;
import com.sunrisedental.dao.IPatientDAO;
import com.sunrisedental.factory.DAOFactory;
import com.sunrisedental.model.DashboardSummaryDTO;
import com.sunrisedental.model.DoctorWorkloadDTO;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public class ReportService {

    private final IPatientDAO patientDAO;
    private final IAppointmentDAO appointmentDAO;
    private final IBillingDAO billingDAO;

    public ReportService() {
        this(
                DAOFactory.getInstance().getPatientDAO(),
                DAOFactory.getInstance().getAppointmentDAO(),
                DAOFactory.getInstance().getBillingDAO()
        );
    }

    public ReportService(IPatientDAO patientDAO, IAppointmentDAO appointmentDAO, IBillingDAO billingDAO) {
        this.patientDAO = patientDAO;
        this.appointmentDAO = appointmentDAO;
        this.billingDAO = billingDAO;
    }

    public DashboardSummaryDTO getDashboardMetrics() {
        DashboardSummaryDTO dto = new DashboardSummaryDTO();
        dto.setTotalPatients(patientDAO.countTotalPatients());
        dto.setTotalAppointments(appointmentDAO.countTotalAppointments());
        dto.setTodayAppointments(appointmentDAO.countTodayAppointments());
        dto.setCompletedAppointments(appointmentDAO.countByStatus("COMPLETED"));
        dto.setPendingAppointments(appointmentDAO.countByStatus("SCHEDULED"));
        dto.setTodayRevenue(billingDAO.calculateTodayRevenue());
        dto.setMonthlyRevenue(billingDAO.calculateMonthlyRevenue());
        dto.setRecentAppointments(appointmentDAO.findRecentAppointments(6));
        return dto;
    }

    public List<DoctorWorkloadDTO> getDoctorWorkload() {
        return billingDAO.getDoctorWorkloadReport();
    }

    public BigDecimal getRevenueBetweenDates(Date startDate, Date endDate) {
        return billingDAO.calculateTotalRevenue(startDate, endDate);
    }
}
