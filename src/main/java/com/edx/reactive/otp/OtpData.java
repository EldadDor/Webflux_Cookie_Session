package com.edx.reactive.otp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpData {
    private String phoneNr;
    private Integer sendType;
    private String password;
    private String clientId;


}
