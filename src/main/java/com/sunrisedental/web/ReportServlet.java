package com.sunrisedental.web;

import com.sunrisedental.model.DashboardSummaryDTO;
import com.sunrisedental.model.DoctorWorkloadDTO;
import com.sunrisedental.service.NotificationService;
import com.sunrisedental.service.ReportService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "ReportServlet", urlPatterns = {"/api/reports/*"})
public class ReportServlet extends BaseServlet {

    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if ("/dashboard".equals(pathInfo) || pathInfo == null || "/".equals(pathInfo)) {
            DashboardSummaryDTO dashboard = reportService.getDashboardMetrics();
            sendSuccess(resp, "Dashboard metrics retrieved", dashboard);
            return;
        }

        if ("/doctor-workload".equals(pathInfo)) {
            List<DoctorWorkloadDTO> workload = reportService.getDoctorWorkload();
            sendSuccess(resp, "Doctor workload report", workload);
            return;
        }

        if ("/revenue".equals(pathInfo)) {
            String startStr = req.getParameter("start");
            String endStr = req.getParameter("end");

            LocalDate start = (startStr != null && !startStr.isEmpty()) ? LocalDate.parse(startStr) : LocalDate.now().minusMonths(1);
            LocalDate end = (endStr != null && !endStr.isEmpty()) ? LocalDate.parse(endStr) : LocalDate.now();

            BigDecimal totalRevenue = reportService.getRevenueBetweenDates(Date.valueOf(start), Date.valueOf(end));
            Map<String, Object> revData = new HashMap<>();
            revData.put("startDate", start.toString());
            revData.put("endDate", end.toString());
            revData.put("totalRevenue", totalRevenue);

            sendSuccess(resp, "Revenue report generated", revData);
            return;
        }

        if ("/notifications".equals(pathInfo)) {
            List<String> logs = NotificationService.getRecentNotifications();
            sendSuccess(resp, "Simulated SMS and Email notification dispatch logs", logs);
            return;
        }

        sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Report endpoint not found.");
    }
}
