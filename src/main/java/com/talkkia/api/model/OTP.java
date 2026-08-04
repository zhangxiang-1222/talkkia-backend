package com.talkkia.api.model;

public class OTP {
    private int otp;
    private long timestamp;
    private static final long OTP_VALIDITY_DURATION = 5 * 60 * 1000; // 5 minutes

    // Default constructor
    public OTP() {}

    public OTP(int otp, long timestamp) {
        this.otp = otp;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public int getOtp() {
        return otp;
    }

    public void setOtp(int otp) {
        this.otp = otp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() - timestamp) > OTP_VALIDITY_DURATION;
    }
}