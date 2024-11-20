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
import com.edx.reactive.common.WebConstants;
import com.edx.reactive.utils.ReactiveRequestContextHolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.netty.util.internal.StringUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Service
@CookieScoped
public class OtpServiceImp2 implements OtpService2 {

    private static final int passwordLength = 6;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    /*@Autowired
    private OtpServiceUtil otpServiceUtil;*/
    @CookieSession
    private OtpLoginData otpLoginData;
    private OtpLoginService  OtpLoginService;
    private Map<SendType,OtpProvider> providersMap;

    @PostConstruct
    public void initOtpServiceImp2() {
//        Logger.info("");
    }

    @Override
    public Mono<OtpSendResult> sendPassword(Integer sendType, String clientId, OtpRoles role, String phoneNr) {
        return ReactiveRequestContextHolder.getExchange()
                .flatMap(serverWebExchange -> {
                    otpLoginData.setClientId(clientId);
                    otpLoginData.setPhoneNr(phoneNr);
                    otpLoginData.setRole(role);
                    ServerHttpRequest request = serverWebExchange.getRequest();
                    ServerHttpResponse response = serverWebExchange.getResponse();

                    String query = request.getURI().getQuery();
                    if (query != null && query.contains(WebConstants.REFERER_URL_PREFIX)) {
                        String referer = query.split(WebConstants.REFERER_URL_PREFIX)[1];
                        String host = URI.create(referer).getHost();
                        if (host != null) {
                            return Mono.error(new OtpServiceException("return to with different host", HttpStatus.OK, OtpErrors.GENERAL_ERROR));
                        }
                        otpLoginData.setReferer(referer);
                    }

                    Map<String, String> headersMap = request.getHeaders().toSingleValueMap();
                    String host = headersMap.get("X-Forwarded-Host");
                    otpLoginData.setBrand("BITUH_ISHIR");
//                    otpLoginData.setRequestIp(HttpRequestUtil.getRequestIP(request));

                    return sendOtpPassword(sendType, otpLoginData)
                            .flatMap(otpSendResult -> {
                                if (!StringUtil.isNullOrEmpty(clientId)) {
                                    boolean isClientCompanyWorker = true;
                                    otpSendResult.setClientCompanyWorker(isClientCompanyWorker);
                                }
                                prepareSessionData(response, otpLoginData);
                                return Mono.just(otpSendResult);
                            });
                });
    }

    private Mono<OtpSendResult> sendOtpPassword(Integer sendType, OtpLoginData otpLoginData) {
        return Mono.fromCallable(() -> {
            OtpProvider otpProvider = providersMap.get(SendType.fromValue(sendType));
            String password = generatePassword();
            OtpPasswordModel otpPassword = new OtpPasswordModel(password, LocalDateTime.now().plusMinutes(OtpServiceUtil.passwordExpirationInMinutes));
            otpLoginData.setSentPassword(otpPassword);
            return otpProvider.send(otpLoginData, password);
        });
    }

    @Override
    public Mono<Boolean> verifyPassword(OtpData otpData, OtpRoles role) {
        return ReactiveRequestContextHolder.getExchange()
                .flatMap(exchange -> {
                    // First check if client is locked
                 /*   if (otpServiceUtil.isClientLocked(otpLoginData.getClientId())) {
                        return Mono.error(new OtpServiceException(
                                "לקוח חסום",
                                HttpStatus.OK,
                                OtpErrors.CLIENT_IS_LOCKED));
                    }*/

                    // Verify OTP password
                    return Mono.fromCallable(() ->
                                    verifyOtpPassword(otpData, otpLoginData, BrandEnum.getByValue(otpLoginData.getBrand())))
                            .flatMap(result -> {
                                if (result != null) {
                                    return Mono.error(new OtpServiceException(
                                            result.getMsg(),
                                            HttpStatus.OK,
                                            result));
                                }

                                // Prepare session data
                                prepareSessionData(exchange.getResponse(), otpLoginData);

                                // Handle client ID case
                                if (otpLoginData.getClientId() != null) {
                                    String token = createAuthToken(otpLoginData);
                                    OtpLoginService.addOtpCookies(
                                            exchange.getResponse(),
                                            token,
                                            otpLoginData.getClientId(),
                                            role.name());
                                }

                                // Handle phone number case
                                if (otpLoginData.getPhoneNr() != null) {
                                    OtpLoginService.addOtpCookies(
                                            exchange.getResponse(),
                                            UUID.randomUUID().toString(),
                                            otpLoginData.getClientId(),
                                            role.name());
                                }

                                // Handle referer case
                                if (otpLoginData.getReferer() != null) {
                                    exchange.getResponse()
                                            .getHeaders()
                                            .setLocation(URI.create(otpLoginData.getReferer()));
                                }

                                return Mono.just(true);
                            });
                });
    }

    /*private Mono<OtpSendResult> sendOtpPassword(Integer sendType, OtpLoginData otpLoginData) {
       return Mono.fromCallable(() -> {
          OtpProvider otpProvider = providersMap.get(SendType.fromValue(sendType));
          String password = generatePassword();
          OtpPasswordModel otpPassword = new OtpPasswordModel(password, LocalDateTime.now().plusMinutes(OtpServiceUtil.passwordExpirationInMinutes));
          otpLoginData.setSentPassword(otpPassword);
          return otpProvider.send(otpLoginData, password);
       });
    }
*/

    private OtpErrors verifyOtpPassword(OtpData otpData, OtpLoginData otpLoginData, String brand) {
        if (otpLoginData == null || otpLoginData.getSentPassword() == null) {
//            Logger.info("otpLoginData error");
            return OtpErrors.GENERAL_ERROR;
        }

        boolean bypassPasswordCheck = false;

    /* if (bypassPasswordCheck) {
          Logger.info(String.format("skipping password validation for client=[%s]", otpLoginData.getClientId()));
       }*/

        if (!bypassPasswordCheck && !otpLoginData.getSentPassword().isPasswordValid(otpData.getPassword())) {
            otpLoginData.countTry();

//            Logger.info(String.format("verify password fail for client=[%s] reason=[password [%s] is not valid]. try %d", otpLoginData.getClientId(), otpData.getPassword(), otpLoginData.getNumOfTries()));

          /*if (otpServiceUtil.isMaxTriesExceeded(otpLoginData.getNumOfTries(), otpLoginData.getClientId())) {
             otpServiceUtil.lockClientTempPass(otpLoginData.getClientId(), brand);
             return OtpErrors.CLIENT_IS_LOCKED;
          }*/
            return OtpErrors.WRONG_PASSWORD;
        }

        if (otpLoginData.getSentPassword().isPasswordExpired()) {
//            Logger.info(String.format("verify password fail for client=[%s] reason=[the password [%s] expiration date has passed]", otpLoginData.getClientId(), otpData.getPassword()));
            return OtpErrors.PASSWORD_EXPIRED;
        }

//        Logger.info(String.format("the password [%s] is valid for client=[%s]", otpData.getPassword(), otpLoginData.getClientId()));

        return null;
    }

    private String createAuthToken(OtpLoginData otpLoginData) {
        String token = "TokenForTesting";
        if (token == null) {
//            Logger.info(String.format("verify password fail for client=[%s] and role=[%s] reason=[token was not created]", otpLoginData.getClientId(), otpLoginData.getRole().name()));
//            throw new OtpServiceException("כישלון ביצירת טוקן", HttpStatus.OK, OtpErrors.AUTH_TOKEN_ERROR);
        }
//        Logger.info(String.format("otp verification success for client=[%s]", otpLoginData.getClientId()));
        return token;
    }

    private String generatePassword() {
        return (String) IntStream.rangeClosed(1, passwordLength).map((x) -> {
            return (new Random()).nextInt(9) + 1;
        }).boxed().map(String::valueOf).collect(Collectors.joining());
    }

    private void prepareSessionData(ServerHttpResponse response, OtpLoginData otpLoginData) {
//        String jsonStr = objectMapper.writeValueAsString(otpLoginData);
//        Pair<String, String> encryptedData = OtpServiceUtil.encryptData(jsonStr);
//        OtpLoginService.addCookie(response, WebConstants.SESSION_DATA, URLEncoder.encode(encryptedData.getFirst(), StandardCharsets.UTF_8));
//        OtpLoginService.addCookie(response, WebConstants.SESSION_KEY, encryptedData.getSecond());
//        OtpLoginService.addCookie(response, WebConstants.AUTH_CLIENT, otpLoginData.getClientId());
//        OtpLoginService.addCookie(response, WebConstants.AUTH_PHONE, otpLoginData.getPhoneNr());
    }


    /*private void removeSessionData(ServerHttpRequest request) {
       OtpLoginService.removeCookie(request, WebConstants.SESSION_DATA);
       OtpLoginService.removeCookie(request, WebConstants.SESSION_KEY);
    }*/

    private OtpLoginData getSessionData(ServerHttpRequest request) {
        Map<String, HttpCookie> cookieMap = request.getCookies().toSingleValueMap();
        if (cookieMap.isEmpty()) {
            throw new RuntimeException("cookie map is empty");
        }
        String sessionData = URLDecoder.decode(cookieMap.get(WebConstants.SESSION_DATA).getValue(), StandardCharsets.UTF_8);
        String sessionKey = cookieMap.get(WebConstants.SESSION_KEY).getValue();
        String jsonSessionData = OtpServiceUtil.decryptData(sessionKey, sessionData);
        OtpLoginData otpLoginData = null;
        try {
            otpLoginData = objectMapper.readValue(jsonSessionData, OtpLoginData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("parse json to OtpLoginData failed", e.getCause());
        }
        return otpLoginData;
    }


}

