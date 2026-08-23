package com.sunrisedental;

import com.sunrisedental.dao.IPatientDAO;
import com.sunrisedental.model.Patient;
import com.sunrisedental.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Task C: Patient Validation Unit Tests")
public class PatientValidationTest {

    private IPatientDAO mockPatientDAO;
    private PatientService patientService;

    @BeforeEach
    void setUp() {
        mockPatientDAO = Mockito.mock(IPatientDAO.class);
        patientService = new PatientService(mockPatientDAO);
    }

    @Test
    @DisplayName("TC-PAT-01: Valid patient data validation")
    void testValidPatient() {
        Patient p = new Patient(0, "PT-1005", "Sanduni Perera", "Colombo 07", "0771234567", "sanduni@gmail.com", Date.valueOf("1996-04-12"), "FEMALE");
        StringBuilder error = new StringBuilder();

        boolean valid = patientService.validatePatient(p, error);

        assertTrue(valid);
        assertEquals(0, error.length());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0771234567", "0714567890", "+94771234567", "0112345678"})
    @DisplayName("TC-PAT-02: Valid phone number formats")
    void testValidPhoneNumbers(String phone) {
        Patient p = new Patient(0, "PT-1005", "Valid Name", "Address", phone, "test@test.com", Date.valueOf("1990-01-01"), "MALE");
        StringBuilder error = new StringBuilder();

        boolean valid = patientService.validatePatient(p, error);

        assertTrue(valid, "Phone format " + phone + " should be valid");
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "phone_number", "077-1234", "07712345678900"})
    @DisplayName("TC-PAT-03: Invalid phone number formats rejected")
    void testInvalidPhoneNumbers(String invalidPhone) {
        Patient p = new Patient(0, "PT-1005", "Valid Name", "Address", invalidPhone, "test@test.com", Date.valueOf("1990-01-01"), "MALE");
        StringBuilder error = new StringBuilder();

        boolean valid = patientService.validatePatient(p, error);

        assertFalse(valid, "Phone format " + invalidPhone + " must be rejected");
        assertTrue(error.toString().contains("contact number"));
    }

    @Test
    @DisplayName("TC-PAT-04: Missing name or name too short")
    void testInvalidName() {
        Patient p = new Patient(0, "PT-1005", "A", "Address", "0771234567", "test@test.com", Date.valueOf("1990-01-01"), "MALE");
        StringBuilder error = new StringBuilder();

        boolean valid = patientService.validatePatient(p, error);

        assertFalse(valid);
        assertTrue(error.toString().contains("at least 2 characters"));
    }

    @Test
    @DisplayName("TC-PAT-05: Missing address rejection")
    void testMissingAddress() {
        Patient p = new Patient(0, "PT-1005", "Valid Name", "", "0771234567", "test@test.com", Date.valueOf("1990-01-01"), "MALE");
        StringBuilder error = new StringBuilder();

        boolean valid = patientService.validatePatient(p, error);

        assertFalse(valid);
        assertTrue(error.toString().contains("address is required"));
    }
}
