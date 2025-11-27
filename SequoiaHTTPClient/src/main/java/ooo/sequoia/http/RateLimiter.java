// Provided by Sequoia Mod
package ooo.sequoia.http;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RateLimiter {
    private static final Logger LOGGER = Logger.getLogger(RateLimiter.class.getName());

    private final Semaphore concurrencySemaphore;
    private final double capacity;
    private double tokens;
    private final double refillRate;
    private long lastRefillTime;

    public RateLimiter(int maxConcurrentRequests, double requestsPerMinute) {
        concurrencySemaphore = new Semaphore(maxConcurrentRequests);
        capacity = requestsPerMinute;
        tokens = requestsPerMinute;
        refillRate = requestsPerMinute / 60000.0;
        lastRefillTime = System.currentTimeMillis();
    }

    private void waitForToken() {
        synchronized (this) {
            while (tokens < 1) {
                refillTokens();
                if (tokens >= 1) {
                    tokens -= 1;
                    return;
                }
                long sleepTime = (long) Math.ceil((1 - tokens) / refillRate);
                try {
                    wait(sleepTime);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    LOGGER.log(Level.WARNING, "Interrupted while waiting for rate limiter", exception);
                    return;
                }
            }
            tokens -= 1;
        }
    }

    private synchronized void refillTokens() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRefillTime;
        if (elapsed > 0) {
            tokens = Math.min(capacity, tokens + elapsed * refillRate);
            lastRefillTime = now;
            notifyAll();
        }
    }

    public void acquire() {
        try {
            waitForToken();
            concurrencySemaphore.acquire();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Interrupted while waiting for rate limiter", exception);
        }
    }

    public void release() {
        concurrencySemaphore.release();
    }
}
