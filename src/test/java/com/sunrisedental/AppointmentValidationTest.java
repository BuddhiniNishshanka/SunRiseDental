package com.sunrisedental;

import com.sunrisedental.dao.*;
import com.sunrisedental.model.*;
import com.sunrisedental.service.AppointmentService;
import com.sunrisedental.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Task C: Appointment Scheduling & Validation TDD Unit Tests")
public class AppointmentValidationTest {

    private IAppointmentDAO mockAppointmentDAO;
    private IPatientDAO mockPatientDAO;
    private IDentistDAO mockDentistDAO;
    private ITreatmentDAO mockTreatmentDAO;
    private IAuditLogDAO mockAuditLogDAO;
    private PatientService mockPatientService;
    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        mockAppointmentDAO = Mockito.mock(IAppointmentDAO.class);
        mockPatientDAO = Mockito.mock(IPatientDAO.class);
        mockDentistDAO = Mockito.mock(IDentistDAO.class);
        mockTreatmentDAO = Mockito.mock(ITreatmentDAO.class);
        mockAuditLogDAO = Mockito.mock(IAuditLogDAO.class);
        PatientService patientService = new PatientService(mockPatientDAO);

        appointmentService = new AppointmentService(
                mockAppointmentDAO, mockPatientDAO, mockDentistDAO, mockTreatmentDAO, mockAuditLogDAO, patientService
        );
    }

    @Test
    @DisplayName("TC-APP-01: Reject appointment with date in the past")
    void testPastDateRejection() {
        Date pastDate = Date.valueOf(LocalDate.now().minusDays(1));
        Time time = Time.valueOf("10:00:00");
        StringBuilder error = new StringBuilder();

        boolean valid = appointmentService.validateAppointmentSchedule(pastDate, time, 1, null, error);

        assertFalse(valid, "Appointment date in the past must be rejected");
        assertTrue(error.toString().contains("past"), "Error message must indicate past date");
    }

    @Test
    @DisplayName("TC-APP-02: Reject appointment on doctor double-booking conflict")
    void testDoubleBookingConflict() {
        Date futureDate = Date.valueOf(LocalDate.now().plusDays(2));
        Time time = Time.valueOf("09:30:00");
        int dentistId = 2;

        when(mockAppointmentDAO.hasConflict(dentistId, futureDate, time, null)).thenReturn(true);

        StringBuilder error = new StringBuilder();
        boolean valid = appointmentService.validateAppointmentSchedule(futureDate, time, dentistId, null, error);

        assertFalse(valid, "Double booking on same doctor slot must be rejected");
        assertTrue(error.toString().contains("already booked"));
    }

    @Test
    @DisplayName("TC-APP-03: Accept valid appointment schedule")
    void testValidSchedule() {
        Date futureDate = Date.valueOf(LocalDate.now().plusDays(3));
        Time time = Time.valueOf("11:00:00");
        int dentistId = 1;

        when(mockAppointmentDAO.hasConflict(dentistId, futureDate, time, null)).thenReturn(false);

        StringBuilder error = new StringBuilder();
        boolean valid = appointmentService.validateAppointmentSchedule(futureDate, time, dentistId, null, error);

        assertTrue(valid, "Valid schedule without conflict should pass validation");
        assertEquals(0, error.length());
    }

    @Test
    @DisplayName("TC-APP-04: Successful new appointment booking flow")
    void testBookNewAppointmentSuccess() {
        Patient patient = new Patient(1, "PT-1001", "Nimal Silva", "Colombo", "0771122334", "nimal@gmail.com", Date.valueOf("1995-05-10"), "MALE");
        Dentist dentist = new Dentist(1, "Dr. Silva", "Surgeon", "101", "0771234567", "doc@clinic.lk", new BigDecimal("3000.00"), true);
        Treatment treatment = new Treatment(1, "TRT-001", "Cleaning", "Scaling", new BigDecimal("3500.00"), "GENERAL");

        Date appDate = Date.valueOf(LocalDate.now().plusDays(1));
        Time appTime = Time.valueOf("10:00:00");

        when(mockPatientDAO.findById(1)).thenReturn(Optional.of(patient));
        when(mockDentistDAO.findById(1)).thenReturn(Optional.of(dentist));
        when(mockTreatmentDAO.findById(1)).thenReturn(Optional.of(treatment));
        when(mockAppointmentDAO.hasConflict(1, appDate, appTime, null)).thenReturn(false);
        when(mockAppointmentDAO.generateNextAppointmentNo()).thenReturn("APP-2026-099");
        when(mockAppointmentDAO.createAppointment(any())).thenReturn(true);

        Appointment createdApp = new Appointment(99, "APP-2026-099", 1, 1, 1, appDate, appTime, "SCHEDULED", "Regular checkup");
        createdApp.setPatientName("Nimal Silva");
        createdApp.setContactNo("0771122334");
        createdApp.setDentistName("Dr. Silva");
        createdApp.setTreatmentName("Cleaning");
        when(mockAppointmentDAO.findById(anyInt())).thenReturn(Optional.of(createdApp));

        StringBuilder error = new StringBuilder();
        Optional<Appointment> result = appointmentService.bookNewAppointment(
                patient, 1, 1, appDate, appTime, "Regular checkup", "reception", error
        );

        assertTrue(result.isPresent(), "Appointment booking must succeed");
        assertEquals("APP-2026-099", result.get().getAppointmentNo());
    }

    @Test
    @DisplayName("TC-APP-05: Search Appointment by unique appointment number")
    void testSearchByAppointmentNo() {
        Appointment mockApp = new Appointment(1, "APP-2026-001", 1, 1, 1, Date.valueOf("2026-08-24"), Time.valueOf("09:00:00"), "SCHEDULED", "Notes");
        when(mockAppointmentDAO.findByAppointmentNo("APP-2026-001")).thenReturn(Optional.of(mockApp));

        Optional<Appointment> result = appointmentService.searchByAppointmentNo("APP-2026-001");

        assertTrue(result.isPresent());
        assertEquals("APP-2026-001", result.get().getAppointmentNo());
    }
}
