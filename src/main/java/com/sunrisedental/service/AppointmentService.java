package com.sunrisedental.service;

import com.sunrisedental.dao.*;
import com.sunrisedental.factory.DAOFactory;
import com.sunrisedental.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class AppointmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentService.class);

    private final IAppointmentDAO appointmentDAO;
    private final IPatientDAO patientDAO;
    private final IDentistDAO dentistDAO;
    private final ITreatmentDAO treatmentDAO;
    private final IAuditLogDAO auditLogDAO;
    private final PatientService patientService;

    public AppointmentService() {
        this(
                DAOFactory.getInstance().getAppointmentDAO(),
                DAOFactory.getInstance().getPatientDAO(),
                DAOFactory.getInstance().getDentistDAO(),
                DAOFactory.getInstance().getTreatmentDAO(),
                DAOFactory.getInstance().getAuditLogDAO(),
                new PatientService()
        );
    }

    public AppointmentService(IAppointmentDAO appointmentDAO, IPatientDAO patientDAO,
                              IDentistDAO dentistDAO, ITreatmentDAO treatmentDAO,
                              IAuditLogDAO auditLogDAO, PatientService patientService) {
        this.appointmentDAO = appointmentDAO;
        this.patientDAO = patientDAO;
        this.dentistDAO = dentistDAO;
        this.treatmentDAO = treatmentDAO;
        this.auditLogDAO = auditLogDAO;
        this.patientService = patientService;
    }

    public boolean validateAppointmentSchedule(Date date, Time time, int dentistId, Integer excludeId, StringBuilder error) {
        if (date == null) {
            error.append("Appointment date is required. ");
            return false;
        }

        LocalDate today = LocalDate.now();
        if (date.toLocalDate().isBefore(today)) {
            error.append("Appointment date cannot be scheduled in the past. ");
            return false;
        }

        if (time == null) {
            error.append("Appointment time is required. ");
            return false;
        }

        if (dentistId <= 0) {
            error.append("A valid dentist must be selected. ");
            return false;
        }

        // Conflict check
        if (appointmentDAO.hasConflict(dentistId, date, time, excludeId)) {
            error.append("Dentist is already booked for this specific time slot. Please choose another time or dentist. ");
            return false;
        }

        return true;
    }

    public Optional<Appointment> bookNewAppointment(
            Patient patientData,
            int dentistId,
            int treatmentId,
            Date appointmentDate,
            Time appointmentTime,
            String notes,
            String operatorUsername,
            StringBuilder errorMessage
    ) {
        // Step 1: Ensure patient exists or create new patient
        Optional<Patient> patientOpt;
        if (patientData.getId() > 0) {
            patientOpt = patientDAO.findById(patientData.getId());
        } else {
            patientOpt = patientService.registerPatient(patientData, errorMessage);
        }

        if (patientOpt.isEmpty()) {
            if (errorMessage.length() == 0) errorMessage.append("Failed to resolve patient details. ");
            return Optional.empty();
        }

        Patient patient = patientOpt.get();

        // Step 2: Validate dentist & treatment
        Optional<Dentist> dentistOpt = dentistDAO.findById(dentistId);
        if (dentistOpt.isEmpty()) {
            errorMessage.append("Selected dentist was not found. ");
            return Optional.empty();
        }

        Optional<Treatment> treatmentOpt = treatmentDAO.findById(treatmentId);
        if (treatmentOpt.isEmpty()) {
            errorMessage.append("Selected treatment was not found. ");
            return Optional.empty();
        }

        // Step 3: Validate appointment date/time and double-booking conflict
        if (!validateAppointmentSchedule(appointmentDate, appointmentTime, dentistId, null, errorMessage)) {
            return Optional.empty();
        }

        // Step 4: Build Appointment Entity
        String appointmentNo = appointmentDAO.generateNextAppointmentNo();
        Appointment app = new Appointment();
        app.setAppointmentNo(appointmentNo);
        app.setPatientId(patient.getId());
        app.setDentistId(dentistId);
        app.setTreatmentId(treatmentId);
        app.setAppointmentDate(appointmentDate);
        app.setAppointmentTime(appointmentTime);
        app.setStatus("SCHEDULED");
        app.setNotes(notes);

        boolean created = appointmentDAO.createAppointment(app);
        if (created) {
            // Retrieve full appointment record with joins
            Optional<Appointment> createdApp = appointmentDAO.findById(app.getId());
            if (createdApp.isPresent()) {
                Appointment fullApp = createdApp.get();

                // Dispatch Observer Notification (SMS/Email)
                NotificationService.notifyAppointmentBooked(fullApp);

                // Audit Log
                auditLogDAO.logAction(new AuditLog(
                        operatorUsername != null ? operatorUsername : "system",
                        "APPOINTMENT_CREATED",
                        "Created appointment " + appointmentNo + " for patient " + patient.getFullName(),
                        "127.0.0.1"
                ));

                return Optional.of(fullApp);
            }
            return Optional.of(app);
        } else {
            errorMessage.append("Database error occurred while persisting appointment.");
            return Optional.empty();
        }
    }

    public Optional<Appointment> searchByAppointmentNo(String appointmentNo) {
        if (appointmentNo == null || appointmentNo.trim().isEmpty()) {
            return Optional.empty();
        }
        return appointmentDAO.findByAppointmentNo(appointmentNo.trim());
    }

    public List<Appointment> searchAppointments(String query) {
        return appointmentDAO.searchAppointments(query);
    }

    public List<Appointment> getAllAppointments() {
        return appointmentDAO.findAll();
    }

    public List<Appointment> getTodayAppointments() {
        return appointmentDAO.findTodayAppointments();
    }

    public boolean cancelAppointment(int id, String operatorUsername) {
        boolean updated = appointmentDAO.updateStatus(id, "CANCELLED");
        if (updated) {
            auditLogDAO.logAction(new AuditLog(
                    operatorUsername != null ? operatorUsername : "system",
                    "APPOINTMENT_CANCELLED",
                    "Cancelled appointment ID " + id,
                    "127.0.0.1"
            ));
        }
        return updated;
    }
}
