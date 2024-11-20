package com.edx.reactive.otp;

import org.springframework.http.HttpStatus;

public class OtpServiceException extends RuntimeException {
    public OtpServiceException(Object msg, HttpStatus httpStatus, Object p2) {
    }
}
