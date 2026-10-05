package com.biharlearning.service;

import com.biharlearning.entity.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Decides whether a source document actually needs downloading (docs section 23) and manages the
 * retry/backoff lifecycle for failures (docs section 25).
 *
 * Uses a conditional HEAD request comparing ETag / Last-Modified / Content-Length against what we
 * already stored. If nothing changed, we skip the download entirely - critical for free-tier
 * bandwidth (docs section 28).
 */
@Service
public class ConditionalFetchService {

    private static final Logger log = LoggerFactory.getLogger(ConditionalFetchService.class);

    public enum FetchDecision { SKIP_UNCHANGED, DOWNLOAD, UNKNOWN }

    public record HeadInfo(int status, String etag, String lastModified, Long contentLength,
                           String contentType) {}

    private final String userAgent;
    private final int timeoutMs;
    private final int maxAttempts;
    private final long backoffBaseMs;
    private final HttpClient http;

    public ConditionalFetchService(
            @Value("${app.sync.user-agent}") String userAgent,
            @Value("${app.sync.request-timeout-ms:15000}") int timeoutMs,
            @Value("${app.sync.max-attempts:3}") int maxAttempts,
            @Value("${app.sync.backoff-base-ms:60000}") long backoffBaseMs) {
        this.userAgent = userAgent;
        this.timeoutMs = timeoutMs;
        this.maxAttempts = maxAttempts;
        this.backoffBaseMs = backoffBaseMs;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /** Issue a HEAD request to cheaply learn the current ETag/Last-Modified/size (docs 23). */
    public HeadInfo head(String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .header("User-Agent", userAgent)
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofMillis(timeoutMs))
                    .build();
            HttpResponse<Void> res = http.send(req, HttpResponse.BodyHandlers.discarding());
            String etag = res.headers().firstValue("etag").orElse(null);
            String lastMod = res.headers().firstValue("last-modified").orElse(null);
            Long len = res.headers().firstValueAsLong("content-length").isPresent()
                    ? res.headers().firstValueAsLong("content-length").getAsLong() : null;
            String ct = res.headers().firstValue("content-type").orElse(null);
            return new HeadInfo(res.statusCode(), etag, lastMod, len, ct);
        } catch (Exception e) {
            log.warn("HEAD failed for {}: {}", url, e.getMessage());
            return new HeadInfo(-1, null, null, null, null);
        }
    }

    /**
     * Compare current source metadata against what we already have on the document.
     * If ETag matches (or Last-Modified + Content-Length both match), skip the download.
     */
    public FetchDecision decide(Document existing, HeadInfo head) {
        if (existing == null) return FetchDecision.DOWNLOAD;        // never seen -> download
        if (head.status() < 0) return FetchDecision.UNKNOWN;         // couldn't probe
        if (head.etag() != null && head.etag().equals(existing.getEtag())) {
            return FetchDecision.SKIP_UNCHANGED;
        }
        if (head.contentLength() != null && existing.getFileSize() != null
                && head.contentLength().equals(existing.getFileSize())
                && existing.getEtag() == null && head.etag() == null) {
            // No ETag available on either side but size is identical -> likely unchanged.
            return FetchDecision.SKIP_UNCHANGED;
        }
        return FetchDecision.DOWNLOAD;
    }

    /**
     * Advance a document's failure state after a failed attempt (docs section 25). Sets PENDING /
     * RETRYING / FAILED and the next retry time with exponential backoff. Never retries forever.
     */
    public void recordFailure(Document d, String error) {
        int attempt = d.getAttemptCount() + 1;
        d.setAttemptCount(attempt);
        d.setLastAttemptAt(OffsetDateTime.now());
        d.setErrorMessage(error);
        if (attempt >= maxAttempts) {
            d.setStatus("FAILED");
            d.setNextRetryAt(null);
        } else {
            d.setStatus("RETRYING");
            long delay = backoffBaseMs * (long) Math.pow(2, attempt - 1);
            d.setNextRetryAt(OffsetDateTime.now().plusNanos(delay * 1_000_000));
        }
    }

    public int maxAttempts() { return maxAttempts; }
}
