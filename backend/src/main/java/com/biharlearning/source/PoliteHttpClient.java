package com.biharlearning.source;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Rate-limited, UA-identified HTTP fetcher for source adapters. Government portals observed
 * during research reset non-browser connections, so we identify ourselves, keep timeouts
 * generous, and throttle between calls. We never bypass access controls.
 */
@Component
public class PoliteHttpClient {

    private static final Logger log = LoggerFactory.getLogger(PoliteHttpClient.class);

    private final String userAgent;
    private final long rateLimitMs;
    private final int timeoutMs;
    private volatile long lastRequestAt = 0L;

    public PoliteHttpClient(
            @Value("${app.sync.user-agent}") String userAgent,
            @Value("${app.sync.rate-limit-ms:3000}") long rateLimitMs,
            @Value("${app.sync.request-timeout-ms:15000}") int timeoutMs) {
        this.userAgent = userAgent;
        this.rateLimitMs = rateLimitMs;
        this.timeoutMs = timeoutMs;
    }

    public synchronized Document get(String url) {
        throttle();
        try {
            Connection conn = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .timeout(timeoutMs)
                    .followRedirects(true)
                    .ignoreHttpErrors(true);
            return conn.get();
        } catch (Exception e) {
            log.warn("Fetch failed for {}: {}", url, e.getMessage());
            return null;
        } finally {
            lastRequestAt = System.currentTimeMillis();
        }
    }

    private void throttle() {
        long elapsed = System.currentTimeMillis() - lastRequestAt;
        if (elapsed < rateLimitMs) {
            try { Thread.sleep(rateLimitMs - elapsed); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }
}
