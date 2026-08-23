package com.sunrisedental.web;

import com.google.gson.JsonObject;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Patient;
import com.sunrisedental.service.AppointmentService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "AppointmentServlet", urlPatterns = {"/api/appointments/*"})
public class AppointmentServlet extends BaseServlet {

    private final AppointmentService appointmentService = new AppointmentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if ("/search".equals(pathInfo)) {
            String appNo = req.getParameter("no");
            String query = req.getParameter("query");

            if (appNo != null && !appNo.trim().isEmpty()) {
                Optional<Appointment> appOpt = appointmentService.searchByAppointmentNo(appNo.trim());
                if (appOpt.isPresent()) {
                    sendSuccess(resp, "Appointment found", appOpt.get());
                } else {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "No appointment found matching appointment number: " + appNo);
                }
                return;
            }

            if (query != null && !query.trim().isEmpty()) {
                List<Appointment> results = appointmentService.searchAppointments(query.trim());
                sendSuccess(resp, "Search results", results);
                return;
            }

            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Please specify an appointment number ('no') or search keyword ('query').");
            return;
        }

        String todayParam = req.getParameter("today");
        if ("true".equalsIgnoreCase(todayParam)) {
            sendSuccess(resp, "Today's appointments", appointmentService.getTodayAppointments());
            return;
        }

        sendSuccess(resp, "All appointments", appointmentService.getAllAppointments());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if ("/cancel".equals(pathInfo)) {
            String idStr = req.getParameter("id");
            if (idStr == null || idStr.trim().isEmpty()) {
                JsonObject body = parseRequestJsonObject(req);
                idStr = body.has("id") ? body.get("id").getAsString() : null;
            }

            if (idStr == null || idStr.trim().isEmpty()) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Appointment ID is required to cancel.");
                return;
            }

            try {
                int id = Integer.parseInt(idStr.trim());
                String username = getLoggedInUsername(req);
                boolean success = appointmentService.cancelAppointment(id, username);
                if (success) {
                    sendSuccess(resp, "Appointment cancelled successfully.", null);
                } else {
                    sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to cancel appointment.");
                }
            } catch (NumberFormatException e) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid appointment ID format.");
            }
            return;
        }

        // Register New Appointment (Task 2)
        JsonObject json = parseRequestJsonObject(req);
        if (json == null || json.entrySet().isEmpty()) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body is missing.");
            return;
        }

        try {
            Patient patient = new Patient();
            if (json.has("patientId") && !json.get("patientId").isJsonNull() && json.get("patientId").getAsInt() > 0) {
                patient.setId(json.get("patientId").getAsInt());
            } else {
                patient.setFullName(json.has("patientName") ? json.get("patientName").getAsString() : "");
                patient.setAddress(json.has("address") ? json.get("address").getAsString() : "");
                patient.setContactNo(json.has("contactNo") ? json.get("contactNo").getAsString() : "");
                patient.setEmail(json.has("email") && !json.get("email").isJsonNull() ? json.get("email").getAsString() : "");
                patient.setGender(json.has("gender") ? json.get("gender").getAsString() : "OTHER");
                if (json.has("dob") && !json.get("dob").isJsonNull() && !json.get("dob").getAsString().isEmpty()) {
                    patient.setDob(Date.valueOf(json.get("dob").getAsString()));
                }
            }

            int dentistId = json.has("dentistId") ? json.get("dentistId").getAsInt() : 0;
            int treatmentId = json.has("treatmentId") ? json.get("treatmentId").getAsInt() : 0;
            String dateStr = json.has("appointmentDate") ? json.get("appointmentDate").getAsString() : "";
            String timeStr = json.has("appointmentTime") ? json.get("appointmentTime").getAsString() : "";
            String notes = json.has("notes") && !json.get("notes").isJsonNull() ? json.get("notes").getAsString() : "";

            if (dateStr.isEmpty() || timeStr.isEmpty()) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Appointment date and time are required.");
                return;
            }

            // Normalize time string (e.g. "09:00" -> "09:00:00")
            if (timeStr.length() == 5) {
                timeStr += ":00";
            }

            Date appDate = Date.valueOf(dateStr);
            Time appTime = Time.valueOf(timeStr);

            String username = getLoggedInUsername(req);
            StringBuilder errorMessage = new StringBuilder();

            Optional<Appointment> createdApp = appointmentService.bookNewAppointment(
                    patient, dentistId, treatmentId, appDate, appTime, notes, username, errorMessage
            );

            if (createdApp.isPresent()) {
                sendSuccess(resp, "Appointment registered successfully! Appointment Number: " + createdApp.get().getAppointmentNo(), createdApp.get());
            } else {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, errorMessage.toString());
            }

        } catch (IllegalArgumentException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid date or time format. Expected YYYY-MM-DD and HH:MM.");
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }

    private String getLoggedInUsername(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("username") != null) {
            return (String) session.getAttribute("username");
        }
        return "reception";
    }
}
