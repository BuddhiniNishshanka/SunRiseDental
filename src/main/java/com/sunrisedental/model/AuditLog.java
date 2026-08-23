package com.sunrisedental.model;

import java.sql.Timestamp;

public class AuditLog {
    private int id;
    private String username;
    private String action;
    private String details;
    private String ipAddress;
    private Timestamp timestamp;

    public AuditLog() {}

    public AuditLog(String username, String action, String details, String ipAddress) {
        this.username = username;
        this.action = action;
        this.details = details;
        this.ipAddress = ipAddress;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
