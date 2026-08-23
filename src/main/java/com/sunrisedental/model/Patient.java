package com.sunrisedental.model;

import java.sql.Date;
import java.sql.Timestamp;

public class Patient {
    private int id;
    private String patientCode;
    private String fullName;
    private String address;
    private String contactNo;
    private String email;
    private Date dob;
    private String gender; // 'MALE', 'FEMALE', 'OTHER'
    private Timestamp createdAt;

    public Patient() {}

    public Patient(int id, String patientCode, String fullName, String address, String contactNo, String email, Date dob, String gender) {
        this.id = id;
        this.patientCode = patientCode;
        this.fullName = fullName;
        this.address = address;
        this.contactNo = contactNo;
        this.email = email;
        this.dob = dob;
        this.gender = gender;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPatientCode() { return patientCode; }
    public void setPatientCode(String patientCode) { this.patientCode = patientCode; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Date getDob() { return dob; }
    public void setDob(Date dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
