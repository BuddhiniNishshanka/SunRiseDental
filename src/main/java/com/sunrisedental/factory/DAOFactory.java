package com.sunrisedental.factory;

import com.sunrisedental.dao.*;

/**
 * Factory Design Pattern for Data Access Objects.
 * Centralizes the creation and instantiation of DAO objects across the distributed application.
 */
public class DAOFactory {

    private static volatile DAOFactory instance;

    private final IUserDAO userDAO;
    private final IPatientDAO patientDAO;
    private final IDentistDAO dentistDAO;
    private final ITreatmentDAO treatmentDAO;
    private final IAppointmentDAO appointmentDAO;
    private final IBillingDAO billingDAO;
    private final IAuditLogDAO auditLogDAO;

    private DAOFactory() {
        this.userDAO = new UserDAOImpl();
        this.patientDAO = new PatientDAOImpl();
        this.dentistDAO = new DentistDAOImpl();
        this.treatmentDAO = new TreatmentDAOImpl();
        this.appointmentDAO = new AppointmentDAOImpl();
        this.billingDAO = new BillingDAOImpl();
        this.auditLogDAO = new AuditLogDAOImpl();
    }

    public static DAOFactory getInstance() {
        if (instance == null) {
            synchronized (DAOFactory.class) {
                if (instance == null) {
                    instance = new DAOFactory();
                }
            }
        }
        return instance;
    }

    public IUserDAO getUserDAO() { return userDAO; }
    public IPatientDAO getPatientDAO() { return patientDAO; }
    public IDentistDAO getDentistDAO() { return dentistDAO; }
    public ITreatmentDAO getTreatmentDAO() { return treatmentDAO; }
    public IAppointmentDAO getAppointmentDAO() { return appointmentDAO; }
    public IBillingDAO getBillingDAO() { return billingDAO; }
    public IAuditLogDAO getAuditLogDAO() { return auditLogDAO; }
}
