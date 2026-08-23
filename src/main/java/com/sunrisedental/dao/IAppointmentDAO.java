package com.sunrisedental.dao;

import com.sunrisedental.model.Appointment;
import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.Optional;

public interface IAppointmentDAO {
    boolean createAppointment(Appointment appointment);
    Optional<Appointment> findById(int id);
    Optional<Appointment> findByAppointmentNo(String appointmentNo);
    List<Appointment> searchAppointments(String query);
    List<Appointment> findAll();
    List<Appointment> findTodayAppointments();
    List<Appointment> findRecentAppointments(int limit);
    boolean hasConflict(int dentistId, Date date, Time time, Integer excludeAppointmentId);
    boolean updateStatus(int id, String status);
    String generateNextAppointmentNo();
    int countTotalAppointments();
    int countTodayAppointments();
    int countByStatus(String status);
}
