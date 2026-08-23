package com.sunrisedental.model;

import java.math.BigDecimal;

public class Dentist {
    private int id;
    private String name;
    private String specialization;
    private String roomNo;
    private String contactNo;
    private String email;
    private BigDecimal consultationFee;
    private boolean available;

    public Dentist() {}

    public Dentist(int id, String name, String specialization, String roomNo, String contactNo, String email, BigDecimal consultationFee, boolean available) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.roomNo = roomNo;
        this.contactNo = contactNo;
        this.email = email;
        this.consultationFee = consultationFee;
        this.available = available;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }

    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public BigDecimal getConsultationFee() { return consultationFee; }
    public void setConsultationFee(BigDecimal consultationFee) { this.consultationFee = consultationFee; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
