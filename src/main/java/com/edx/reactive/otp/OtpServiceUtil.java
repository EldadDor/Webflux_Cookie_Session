package com.edx.reactive.otp;

import org.springframework.stereotype.Service;

@Service
public class OtpServiceUtil {
    public static long passwordExpirationInMinutes;

    public static String decryptData(String idCode, String idMsg) {
        return null;
    }

    public boolean isSmsServiceAvailable() {
        return false;
    }

    public boolean isClientLocked(String clientId) {
        return false;
    }

    public boolean isClientExists(String clientId) {
        return false;
    }
}
