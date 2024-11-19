package com.edx.reactive.otp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class OtpPasswordModel {

    private String password;
    private LocalDateTime passwordExpirationDate;


    @JsonIgnore
    public boolean isPasswordValid(String passwordToCheck) {
        return passwordToCheck != null && passwordToCheck.equals(password);
    }

    @JsonIgnore
    public boolean isPasswordExpired() {
        return LocalDateTime.now().isAfter(passwordExpirationDate);
    }

}