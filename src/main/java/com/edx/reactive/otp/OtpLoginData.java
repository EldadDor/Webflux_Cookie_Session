package com.edx.reactive.otp;

import com.edx.reactive.common.CookieData;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@Component
public class OtpLoginData implements CookieData {
    private String clientId;
    private String phoneNr;
    private OtpRoles role;
    private int numOfTries;
    private OtpPasswordModel sentPassword;
    private String referer;
    private String requestIp;
    private String brand;

    public void countTry() {
        numOfTries++;
    }

    public void resetTries() {
        numOfTries = 0;
    }

    public OtpLoginData(String clientId, String phoneNr, OtpRoles role) {
        this.clientId = clientId;
        this.role = role;
        this.phoneNr = phoneNr;
    }
}

