package com.wanhui.security.rate;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 进程内令牌桶限流器，适用于单实例 MVP。
 * 多实例部署前应替换为 Redis 或网关限流。
 */
public class InMemoryRateLimiter {
    private final int capacity;
    private final long windowMillis;
    private final Clock clock;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public InMemoryRateLimiter(int capacity, long windowMillis) {
        this(capacity, windowMillis, Clock.systemUTC());
    }

    public InMemoryRateLimiter(int capacity, long windowMillis, Clock clock) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        if (windowMillis < 1) {
            throw new IllegalArgumentException("windowMillis must be positive");
        }
        this.capacity = capacity;
        this.windowMillis = windowMillis;
        this.clock = clock;
    }

    public boolean tryAcquire(String key) {
        long now = clock.millis();
        Bucket bucket = buckets.computeIfAbsent(key, ignored -> new Bucket(capacity, now));
        synchronized (bucket) {
            long elapsed = Math.max(0L, now - bucket.lastRefillMillis);
            if (elapsed > 0) {
                double refill = (double) elapsed * capacity / windowMillis;
                bucket.tokens = Math.min(capacity, bucket.tokens + refill);
                bucket.lastRefillMillis = now;
            }
            if (bucket.tokens >= 1.0D) {
                bucket.tokens -= 1.0D;
                return true;
            }
            return false;
        }
    }

    public int trackedKeys() {
        return buckets.size();
    }

    public void reset() {
        buckets.clear();
    }

    private static final class Bucket {
        private double tokens;
        private long lastRefillMillis;

        private Bucket(int capacity, long now) {
            this.tokens = capacity;
            this.lastRefillMillis = now;
        }
    }
}
