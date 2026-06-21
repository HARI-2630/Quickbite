package com.quickbite.security;

import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static final int BUCKET_CAPACITY = 5;
    private static final long REFILL_PERIOD_MS = 12000; // Refill 1 token every 12 seconds (5 per minute)
    
    private static final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    /**
     * Checks if a request from the given IP address is allowed.
     * Consumes 1 token if available.
     */
    public static boolean isAllowed(String ipAddress) {
        TokenBucket bucket = buckets.computeIfAbsent(ipAddress, k -> new TokenBucket(BUCKET_CAPACITY));
        return bucket.consume();
    }

    private static class TokenBucket {
        private final int capacity;
        private double tokens;
        private long lastRefillTime;

        public TokenBucket(int capacity) {
            this.capacity = capacity;
            this.tokens = capacity;
            this.lastRefillTime = System.currentTimeMillis();
        }

        public synchronized boolean consume() {
            refill();
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;
            if (elapsed > 0) {
                double tokensToAdd = (double) elapsed / REFILL_PERIOD_MS;
                tokens = Math.min(capacity, tokens + tokensToAdd);
                lastRefillTime = now;
            }
        }
    }
}
