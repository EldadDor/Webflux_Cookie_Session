package com.edx.reactive.utils;

import com.edx.reactive.common.CookieData;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class CookieDataManager {
    private final Map<String, CookieData> cookieDataMap = new ConcurrentHashMap<>();

    public void setCookieData(String sessionId, CookieData cookieData) {
        cookieDataMap.put(sessionId, cookieData);
    }

    public CookieData getCookieData(String sessionId) {
        return cookieDataMap.get(sessionId);
    }
}
