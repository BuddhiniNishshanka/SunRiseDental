package com.sunrisedental.service;

import com.sunrisedental.dao.IPatientDAO;
import com.sunrisedental.factory.DAOFactory;
import com.sunrisedental.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class PatientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PatientService.class);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(?:\\+94|0)?[0-9]{9,10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private final IPatientDAO patientDAO;

    public PatientService() {
        this(DAOFactory.getInstance().getPatientDAO());
    }

    public PatientService(IPatientDAO patientDAO) {
        this.patientDAO = patientDAO;
    }

    public boolean validatePatient(Patient patient, StringBuilder errorMessage) {
        if (patient == null) {
            errorMessage.append("Patient data cannot be empty. ");
            return false;
        }

        if (patient.getFullName() == null || patient.getFullName().trim().length() < 2) {
            errorMessage.append("Patient name must be at least 2 characters long. ");
        }

        if (patient.getAddress() == null || patient.getAddress().trim().isEmpty()) {
            errorMessage.append("Patient address is required. ");
        }

        if (patient.getContactNo() == null || !PHONE_PATTERN.matcher(patient.getContactNo().trim()).matches()) {
            errorMessage.append("Invalid contact number format (e.g. 0771234567). ");
        }

        if (patient.getEmail() != null && !patient.getEmail().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(patient.getEmail().trim()).matches()) {
                errorMessage.append("Invalid email address format. ");
            }
        }

        if (patient.getGender() == null || patient.getGender().trim().isEmpty()) {
            errorMessage.append("Patient gender is required. ");
        }

        return errorMessage.length() == 0;
    }

    public Optional<Patient> registerPatient(Patient patient, StringBuilder errorMessage) {
        if (!validatePatient(patient, errorMessage)) {
            return Optional.empty();
        }

        // Generate next patient code if not set
        if (patient.getPatientCode() == null || patient.getPatientCode().trim().isEmpty()) {
            patient.setPatientCode(patientDAO.generateNextPatientCode());
        }

        // Check if patient with this contact already exists
        Optional<Patient> existing = patientDAO.findByContactNo(patient.getContactNo().trim());
        if (existing.isPresent()) {
            LOGGER.info("Patient with contact {} already exists: {}", patient.getContactNo(), existing.get().getFullName());
            return existing;
        }

        boolean created = patientDAO.createPatient(patient);
        if (created) {
            LOGGER.info("Registered new patient {} ({})", patient.getFullName(), patient.getPatientCode());
            return Optional.of(patient);
        } else {
            errorMessage.append("Database error occurred while registering patient.");
            return Optional.empty();
        }
    }

    public List<Patient> getAllPatients() {
        return patientDAO.findAll();
    }

    public List<Patient> searchPatients(String query) {
        if (query == null || query.trim().isEmpty()) {
            return patientDAO.findAll();
        }
        return patientDAO.searchPatients(query.trim());
    }

    public Optional<Patient> getPatientById(int id) {
        return patientDAO.findById(id);
    }
}
