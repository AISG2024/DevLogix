package com.aisg.devlogix.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimiterService {
    private static final long REQUEST_INTERVAL_MS = 3000;
    private static final int MAX_REQUESTS = 10;

    private final Map<String, Long> lastRequestTimestamps = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();

    public boolean isRequestAllowed(String username) {
        long currentTime = System.currentTimeMillis();
        
        lastRequestTimestamps.putIfAbsent(username, currentTime);
        requestCounts.putIfAbsent(username, new AtomicInteger(0));

        long lastRequestTime = lastRequestTimestamps.get(username);
        AtomicInteger requestCount = requestCounts.get(username);

        if (currentTime - lastRequestTime < REQUEST_INTERVAL_MS) {
            return false;
        }
        if (requestCount.get() >= MAX_REQUESTS) {
            return false;
        }

        lastRequestTimestamps.put(username, currentTime);
        requestCount.incrementAndGet();

        return true;
    }

    public void resetRequestCount(String username) {
        requestCounts.remove(username);
    }
}