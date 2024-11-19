/*
 * User: nava
 * Date: 04/01/2024
 *
 * Copyright (2005) IDI. All rights reserved.
 * This software is a proprietary information of Israeli Direct Insurance.
 * Created by IntelliJ IDEA.
 */
package com.edx.reactive.otp;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

public interface OtpService2 {

    Mono<OtpSendResult> sendPassword(Integer sendType, String clientId, OtpRoles role, String phoneNr);

    Mono<Boolean> verifyPassword(OtpData otpData, OtpRoles role);
}