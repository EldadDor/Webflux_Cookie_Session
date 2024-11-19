/*
 * User: nava
 * Date: 04/01/2024
 *
 * Copyright (2005) IDI. All rights reserved.
 * This software is a proprietary information of Israeli Direct Insurance.
 * Created by IntelliJ IDEA.
 */
package com.edx.reactive.otp;


import com.edx.reactive.common.CookieScoped;
import com.edx.reactive.common.CookieSession;
import com.edx.reactive.utils.ReactiveRequestContextHolder;
import io.netty.util.internal.StringUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@CookieScoped
public class OtpClientServiceControllerImpl2 implements OtpClientServiceController2 {

    private OtpService2 otpService;
    private OtpServiceUtil otpServiceUtil;

    @CookieSession
    private OtpLoginData otpLoginData;

    @Override
    public Mono<OtpSendResult> sendPassword(String idCode, String idMsg, OtpData otpData) {
        return ReactiveRequestContextHolder.getExchange().flatMap(exchange -> {
            // Validate SMS service
            if (otpData.getSendType() == SendType.SMS.getType() && !otpServiceUtil.isSmsServiceAvailable()) {
                return Mono.error(new OtpServiceException(OtpErrors.SMS_SERVICE_NOT_VALID.getMsg(), HttpStatus.OK, OtpErrors.SMS_SERVICE_NOT_VALID));
            }
            var ref = new Object() {
                String clientId = "";
            };
            if (StringUtil.isNullOrEmpty(ref.clientId) && idCode != null && idMsg != null) {
                ref.clientId = OtpServiceUtil.decryptData(idCode, idMsg);
            } else {
                ref.clientId = otpData.getClientId();
            }

            // Access request headers if needed
            String userAgent = exchange.getRequest().getHeaders().getFirst(HttpHeaders.USER_AGENT);

            // Store data in session if needed
            otpLoginData.setClientId(ref.clientId);

            return validateClient(ref.clientId).flatMap(valid -> otpService.sendPassword(otpData.getSendType(), ref.clientId, OtpRoles.CLIENT, null));
        });
    }


    private Mono<Boolean> validateClient(String clientId) {
        return Mono.defer(() -> {
            OtpErrors otpErrors = null;

            if (StringUtil.isNullOrEmpty(clientId)) {
                otpErrors = OtpErrors.CLIENT_ID_IS_EMPTY;
            } else if (!otpServiceUtil.isClientExists(clientId)) {
                otpErrors = OtpErrors.CLIENT_NOT_EXISTS;
            } else if (otpServiceUtil.isClientLocked(clientId)) {
                otpErrors = OtpErrors.CLIENT_IS_LOCKED;
            }

            if (otpErrors != null) {
//                Logger.error("validation for client=[{}] failed, reason=[{}]", clientId, otpErrors.getMsg());
                return Mono.error(new OtpServiceException(otpErrors.getMsg(), HttpStatus.OK, otpErrors));
            }

            return Mono.just(true);
        });
    }

    @Override
    public Mono<OtpSendResult> sendPasswordForPhone(OtpData otpData) {
        return ReactiveRequestContextHolder.getExchange().flatMap(exchange -> {
            if (StringUtil.isNullOrEmpty(otpData.getPhoneNr())) {
                return Mono.error(new OtpServiceException(OtpErrors.PHONE_NR_IS_EMPTY.getMsg(), HttpStatus.OK, OtpErrors.PHONE_NR_IS_EMPTY));
            }
            return otpService.sendPassword(otpData.getSendType(), null, OtpRoles.CLIENT, otpData.getPhoneNr());
        });
    }

    @Override
    public Mono<Boolean> verify(OtpData otpData) {
        return ReactiveRequestContextHolder.getExchange().flatMap(exchange -> otpService.verifyPassword(otpData, OtpRoles.CLIENT));
    }
}
