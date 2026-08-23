package com.sunrisedental.service;

import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Bill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Observer Pattern Implementation for Notification Dispatching.
 * Simulates enterprise SMS and Email notifications for patient appointments and invoices.
 */
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);
    private static final List<String> NOTIFICATION_DISPATCH_LOG = Collections.synchronizedList(new ArrayList<>());

    public static void notifyAppointmentBooked(Appointment appointment) {
        String smsMessage = String.format(
                "[SMS SENT to %s]: Dear %s, your appointment (#%s) with %s for %s is confirmed for %s at %s. Sunrise Dental Clinic.",
                appointment.getContactNo(),
                appointment.getPatientName(),
                appointment.getAppointmentNo(),
                appointment.getDentistName(),
                appointment.getTreatmentName(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime()
        );

        String emailMessage = String.format(
                "[EMAIL SENT to Patient]: Subject: Appointment Confirmation - %s\n" +
                "Dear %s,\nYour appointment with %s at %s on %s %s has been confirmed.\nLocation: %s, Sunrise Dental Clinic Colombo.",
                appointment.getAppointmentNo(),
                appointment.getPatientName(),
                appointment.getDentistName(),
                appointment.getDentistRoomNo(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getDentistRoomNo()
        );

        NOTIFICATION_DISPATCH_LOG.add(smsMessage);
        NOTIFICATION_DISPATCH_LOG.add(emailMessage);
        LOGGER.info(smsMessage);
    }

    public static void notifyInvoicePaid(Bill bill) {
        String receiptMessage = String.format(
                "[SMS SENT to %s]: Dear %s, receipt %s for LKR %.2f has been generated for your visit with %s. Thank you for choosing Sunrise Dental Clinic.",
                bill.getPatientContact() != null ? bill.getPatientContact() : "Patient",
                bill.getPatientName(),
                bill.getBillNo(),
                bill.getTotalAmount(),
                bill.getDentistName()
        );

        NOTIFICATION_DISPATCH_LOG.add(receiptMessage);
        LOGGER.info(receiptMessage);
    }

    public static List<String> getRecentNotifications() {
        return new ArrayList<>(NOTIFICATION_DISPATCH_LOG);
    }
}
