package com.quickbite.models;

import java.sql.Timestamp;

public class AuditLog {
    private int id;
    private Integer userId;
    private String action;
    private String ipAddress;
    private String userAgent;
    private Timestamp createdAt;

    public AuditLog() {}

    public AuditLog(int id, Integer userId, String action, String ipAddress, String userAgent, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
