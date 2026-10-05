package com.biharlearning.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/**
 * Supabase Storage provider. Talks to the Supabase Storage REST API with the server-side service
 * key (never exposed to the browser). Uploads stream from an InputStream (docs section 11) and
 * public/signed URLs let the browser fetch distributable PDFs directly from the CDN without
 * proxying through Spring Boot (docs section 12).
 *
 * Active when STORAGE_PROVIDER=SUPABASE and SUPABASE_URL + SUPABASE_SERVICE_KEY are set. If they
 * are not configured, canHost() returns false so the pipeline safely falls back to EXTERNAL.
 */
@Component
public class SupabaseStorageProvider implements FileStorageProvider {

    private static final Logger log = LoggerFactory.getLogger(SupabaseStorageProvider.class);

    private final String supabaseUrl;
    private final String serviceKey;
    private final String bucket;
    private final HttpClient http;

    public SupabaseStorageProvider(
            @Value("${app.storage.supabase.url:}") String supabaseUrl,
            @Value("${app.storage.supabase.service-key:}") String serviceKey,
            @Value("${app.storage.bucket:documents}") String bucket) {
        this.supabaseUrl = stripTrailingSlash(supabaseUrl);
        this.serviceKey = serviceKey;
        this.bucket = bucket;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
    }

    @Override
    public String key() { return "SUPABASE"; }

    @Override
    public boolean canHost() {
        return supabaseUrl != null && !supabaseUrl.isBlank()
                && serviceKey != null && !serviceKey.isBlank();
    }

    private String objectUrl(String path) {
        return supabaseUrl + "/storage/v1/object/" + bucket + "/" + encodePath(path);
    }

    @Override
    public Optional<StoredObject> store(String path, InputStream data, long sizeBytes,
                                        String contentType, String checksum) {
        if (!canHost()) return Optional.empty();
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(objectUrl(path)))
                    .header("Authorization", "Bearer " + serviceKey)
                    .header("Content-Type", contentType == null ? "application/octet-stream" : contentType)
                    .header("x-upsert", "true")
                    // Store the checksum as object metadata for integrity + dedup traceability.
                    .header("x-amz-meta-sha256", checksum == null ? "" : checksum)
                    .POST(HttpRequest.BodyPublishers.ofInputStream(() -> data))
                    .timeout(Duration.ofMinutes(5))
                    .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() / 100 == 2) {
                return Optional.of(new StoredObject(key(), bucket, path, sizeBytes, checksum, contentType));
            }
            log.warn("Supabase upload failed ({}): {}", res.statusCode(), truncate(res.body()));
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Supabase upload error for {}: {}", path, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> urlFor(String path, boolean signed, int expirySeconds) {
        if (!canHost()) return Optional.empty();
        if (!signed) {
            // Public bucket object URL (use only for legally distributable content).
            return Optional.of(supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + encodePath(path));
        }
        try {
            String body = "{\"expiresIn\":" + Math.max(expirySeconds, 60) + "}";
            HttpRequest req = HttpRequest.newBuilder(URI.create(
                            supabaseUrl + "/storage/v1/object/sign/" + bucket + "/" + encodePath(path)))
                    .header("Authorization", "Bearer " + serviceKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(20))
                    .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() / 100 == 2) {
                // Response contains {"signedURL":"/object/sign/..."} - prefix with the storage base.
                String signedPath = extractJsonString(res.body(), "signedURL");
                if (signedPath != null) {
                    return Optional.of(supabaseUrl + "/storage/v1" + signedPath);
                }
            }
            log.warn("Supabase sign failed ({})", res.statusCode());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Supabase sign error for {}: {}", path, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<InputStream> open(String path) {
        if (!canHost()) return Optional.empty();
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(objectUrl(path)))
                    .header("Authorization", "Bearer " + serviceKey)
                    .GET()
                    .timeout(Duration.ofMinutes(2))
                    .build();
            HttpResponse<InputStream> res = http.send(req, HttpResponse.BodyHandlers.ofInputStream());
            if (res.statusCode() / 100 == 2) return Optional.of(res.body());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Supabase open error for {}: {}", path, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean delete(String path) {
        if (!canHost()) return false;
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(objectUrl(path)))
                    .header("Authorization", "Bearer " + serviceKey)
                    .DELETE()
                    .timeout(Duration.ofSeconds(30))
                    .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            return res.statusCode() / 100 == 2;
        } catch (Exception e) {
            log.warn("Supabase delete error for {}: {}", path, e.getMessage());
            return false;
        }
    }

    @Override
    public Optional<Long> usedBytes() {
        // Supabase does not expose a simple per-bucket byte total via a single stable public API;
        // we compute usage from our own documents table instead (see StorageService). Return empty
        // so we never invent a figure (docs section 42).
        return Optional.empty();
    }

    // --- helpers ---

    private static String stripTrailingSlash(String s) {
        if (s == null) return null;
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }

    private static String encodePath(String path) {
        // Keep slashes as path separators; encode spaces conservatively.
        return path.replace(" ", "%20");
    }

    private static String truncate(String s) {
        if (s == null) return "";
        return s.length() > 300 ? s.substring(0, 300) + "..." : s;
    }

    private static String extractJsonString(String json, String field) {
        if (json == null) return null;
        String needle = "\"" + field + "\"";
        int i = json.indexOf(needle);
        if (i < 0) return null;
        int colon = json.indexOf(':', i + needle.length());
        if (colon < 0) return null;
        int firstQuote = json.indexOf('"', colon + 1);
        if (firstQuote < 0) return null;
        int secondQuote = json.indexOf('"', firstQuote + 1);
        if (secondQuote < 0) return null;
        return json.substring(firstQuote + 1, secondQuote);
    }
}
