package com.sunrisedental.dao;

import com.sunrisedental.model.Patient;
import java.util.List;
import java.util.Optional;

public interface IPatientDAO {
    Optional<Patient> findById(int id);
    Optional<Patient> findByCode(String patientCode);
    Optional<Patient> findByContactNo(String contactNo);
    List<Patient> searchPatients(String keyword);
    List<Patient> findAll();
    boolean createPatient(Patient patient);
    boolean updatePatient(Patient patient);
    int countTotalPatients();
    String generateNextPatientCode();
}
