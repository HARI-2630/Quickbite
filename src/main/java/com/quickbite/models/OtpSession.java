package com.quickbite.models;

import java.sql.Timestamp;

public class OtpSession {
    private int id;
    private String phoneOrEmail;
    private String otpCode;
    private int resendCount;
    private Timestamp lastRequestedAt;
    private Timestamp expiresAt;
    private boolean verified;

    public OtpSession() {}

    public OtpSession(int id, String phoneOrEmail, String otpCode, int resendCount, Timestamp lastRequestedAt, Timestamp expiresAt, boolean verified) {
        this.id = id;
        this.phoneOrEmail = phoneOrEmail;
        this.otpCode = otpCode;
        this.resendCount = resendCount;
        this.lastRequestedAt = lastRequestedAt;
        this.expiresAt = expiresAt;
        this.verified = verified;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPhoneOrEmail() { return phoneOrEmail; }
    public void setPhoneOrEmail(String phoneOrEmail) { this.phoneOrEmail = phoneOrEmail; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public int getResendCount() { return resendCount; }
    public void setResendCount(int resendCount) { this.resendCount = resendCount; }

    public Timestamp getLastRequestedAt() { return lastRequestedAt; }
    public void setLastRequestedAt(Timestamp lastRequestedAt) { this.lastRequestedAt = lastRequestedAt; }

    public Timestamp getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Timestamp expiresAt) { this.expiresAt = expiresAt; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
}
