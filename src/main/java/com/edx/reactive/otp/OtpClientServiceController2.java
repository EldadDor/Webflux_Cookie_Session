/*
 * User: nava
 * Date: 04/01/2024
 *
 * Copyright (2005) IDI. All rights reserved.
 * This software is a proprietary information of Israeli Direct Insurance.
 * Created by IntelliJ IDEA.
 */
package com.edx.reactive.otp;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;


@RequestMapping(value = "/client2", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.ALL_VALUE})
public interface OtpClientServiceController2 {

    @PostMapping("send")
    Mono<OtpSendResult> sendPassword(@RequestParam(name = "idCode") String idCode, @RequestParam(name = "idMsg") String idMsg, @RequestBody OtpData otpData);

    @PostMapping("send/phone")
    Mono<OtpSendResult> sendPasswordForPhone(@RequestBody OtpData otpData);

    @PostMapping("verify")
    Mono<Boolean> verify(@RequestBody OtpData otpData);


}