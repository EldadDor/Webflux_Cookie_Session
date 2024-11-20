package com.edx.reactive.otp;

import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.edx.reactive.common.WebConstants.*;

@Service
public class OtpLoginService {

    private static int OTP_MAX_AGE = 60 * 20; //20min

    public static void addOtpCookies(ServerHttpResponse response, String token, String client, String role) {
        addCookie(response, AUTH_TOKEN, token, OTP_MAX_AGE);
        addCookie(response, AUTH_CLIENT, client, OTP_MAX_AGE);
        addCookie(response, AUTH_ROLE, role, OTP_MAX_AGE);
    }


    public static void addCookie(ServerHttpResponse response, String key, String value) {
        addCookie(response, key, value, -1);
    }

    public static void addCookie(ServerHttpResponse response, String key, String value, Integer maxAge) {
        response.addCookie(ResponseCookie.from(key, value).maxAge(maxAge).path("/").httpOnly(true).secure(true).build());
    }


    public static void removeTokenCookie(ServerHttpRequest request) {
        removeCookie(request, AUTH_TOKEN);
        removeCookie(request, AUTH_CLIENT);
        removeCookie(request, AUTH_ROLE);
    }

    public static void removeCookie(ServerHttpRequest request, String cookieName) {
        Map<String, org.springframework.http.HttpCookie> cookieMap = request.getCookies().toSingleValueMap();
        HttpCookie cookie = cookieMap.get(cookieName);
        if (cookie != null) {
            ResponseCookie.from(cookie.getName(), cookie.getValue()).maxAge(0).build();
        }
    }

    public static String getCookieValue(ServerHttpRequest request, String cookie) {
        Map<String, HttpCookie> cookieMap = request.getCookies().toSingleValueMap();
        if (!cookieMap.isEmpty()) {
            HttpCookie authCookie = cookieMap.get(cookie);
            if (authCookie != null) {
                return authCookie.getValue();
            }
        }
        return null;
    }

}