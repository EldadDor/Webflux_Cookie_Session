package com.edx.reactive.otp;

public enum OtpErrors {
    PHONE_NR_IS_EMPTY, SMS_SERVICE_NOT_VALID, CLIENT_ID_IS_EMPTY, CLIENT_NOT_EXISTS, CLIENT_IS_LOCKED, GENERAL_ERROR, PASSWORD_EXPIRED, WRONG_PASSWORD;

    public Object getMsg() {
        return null;
    }
}
