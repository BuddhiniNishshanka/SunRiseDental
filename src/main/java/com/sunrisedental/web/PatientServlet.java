package com.sunrisedental.web;

import com.sunrisedental.model.Patient;
import com.sunrisedental.service.PatientService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "PatientServlet", urlPatterns = {"/api/patients/*"})
public class PatientServlet extends BaseServlet {

    private final PatientService patientService = new PatientService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String query = req.getParameter("query");
        String idParam = req.getParameter("id");

        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idParam.trim());
                Optional<Patient> patientOpt = patientService.getPatientById(id);
                if (patientOpt.isPresent()) {
                    sendSuccess(resp, "Patient retrieved", patientOpt.get());
                } else {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Patient not found.");
                }
                return;
            } catch (NumberFormatException e) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid patient ID format.");
                return;
            }
        }

        List<Patient> list = (query != null && !query.trim().isEmpty())
                ? patientService.searchPatients(query)
                : patientService.getAllPatients();

        sendSuccess(resp, "Patients retrieved", list);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Patient patient = parseRequestBody(req, Patient.class);
        if (patient == null) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
            return;
        }

        StringBuilder error = new StringBuilder();
        Optional<Patient> created = patientService.registerPatient(patient, error);
        if (created.isPresent()) {
            sendSuccess(resp, "Patient registered successfully", created.get());
        } else {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, error.toString());
        }
    }
}
