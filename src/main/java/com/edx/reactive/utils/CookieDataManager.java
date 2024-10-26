package com.edx.reactive.utils;

import com.edx.reactive.common.CookieData;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;


@Service
public class CookieDataManager {
    private final Map<String, CookieData> cookieDataMap = new ConcurrentReferenceHashMap<>();

    public void createEmptySession(String sessionId) {
        cookieDataMap.put(sessionId, null);
    }

    public void setCookieData(String sessionId, CookieData cookieData) {
        cookieDataMap.put(sessionId, cookieData);
    }

    public CookieData getCookieData(String sessionId) {
        return cookieDataMap.get(sessionId);
    }

    public boolean containsSession(String sessionId) {
        return cookieDataMap.containsKey(sessionId);
    }

}
