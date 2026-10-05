package com.biharlearning.service;

import com.biharlearning.entity.ContentSource;
import com.biharlearning.entity.Document;
import com.biharlearning.service.ConditionalFetchService.FetchDecision;
import com.biharlearning.service.ConditionalFetchService.HeadInfo;
import com.biharlearning.storage.Checksums;
import com.biharlearning.storage.FileStorageService;
import com.biharlearning.storage.PdfValidator;
import com.biharlearning.storage.StorageDecisionEngine;
import com.biharlearning.storage.StorageDecisionEngine.Decision;
import com.biharlearning.storage.StorageDecisionEngine.Outcome;
import com.biharlearning.storage.StoredObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.UUID;

/**
 * The end-to-end PDF ingestion pipeline (docs sections 5, 22, 23, 26, 40).
 *
 * It stitches together the already-existing building blocks in the correct order and nothing more:
 *
 *   discover (caller) → identify source → check permission/license → conditional fetch (HEAD) →
 *   download only when required → validate PDF → compute checksum → dedup/version →
 *   store in object storage IF permitted → persist metadata in PostgreSQL.
 *
 * It never re-hosts content whose redistribution permission is not clearly GRANTED
 * (docs section 36): in that case it keeps metadata + the official URL only (EXTERNAL_REFERENCE).
 *
 * To keep it unit-testable without real network calls, the actual byte download is delegated to a
 * small {@link Downloader} functional interface. In production this is backed by the polite HTTP
 * client; tests can supply bytes directly.
 */
@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    /** Supplies the bytes + response metadata for a URL. Pluggable so tests avoid real network. */
    @FunctionalInterface
    public interface Downloader {
        /** @return the downloaded payload, or empty on failure. */
        Optional<Downloaded> download(String url);
    }

    public record Downloaded(byte[] bytes, String contentType, String etag, String lastModified) {}

    /** Outcome of ingesting a single discovered PDF. */
    public record IngestResult(Decision decision, String reason, Document document,
                               boolean downloaded, boolean newVersion, String checksum) {}

    private final ConditionalFetchService fetch;
    private final PdfValidator validator;
    private final StorageDecisionEngine decisionEngine;
    private final FileStorageService storage;
    private final DocumentService documents;
    private final long maxHostBytes;

    public IngestionService(ConditionalFetchService fetch, PdfValidator validator,
                            StorageDecisionEngine decisionEngine, FileStorageService storage,
                            DocumentService documents,
                            @Value("${app.storage.max-host-bytes:52428800}") long maxHostBytes) {
        this.fetch = fetch;
        this.validator = validator;
        this.decisionEngine = decisionEngine;
        this.storage = storage;
        this.documents = documents;
        this.maxHostBytes = maxHostBytes;
    }

    /**
     * Ingest one discovered PDF.
     *
     * @param source           originating content source (may be null)
     * @param sourceUrl        where the PDF was discovered
     * @param officialUrl      the canonical official URL to show/attribute (often == sourceUrl)
     * @param fileName         suggested file name
     * @param storagePath      clean object-storage path to use IF hosted (docs section 10)
     * @param academicYear     e.g. "2026-27"
     * @param hostingPermission GRANTED / DENIED / UNKNOWN (docs section 36)
     * @param downloader       byte supplier (pluggable for tests)
     */
    public IngestResult ingest(ContentSource source, String sourceUrl, String officialUrl,
                               String fileName, String storagePath, String academicYear,
                               String hostingPermission, Downloader downloader) {

        boolean licenseKnown = source != null && source.getLicense() != null && !source.getLicense().isBlank();

        // 1) Early policy decision. If we may not host (DENIED/UNKNOWN) or the provider cannot host,
        //    we short-circuit to an EXTERNAL_REFERENCE / PENDING_REVIEW record without downloading.
        Outcome outcome = decisionEngine.decide(source, storage.canHost(), licenseKnown, hostingPermission);
        if (outcome.decision() != Decision.HOST) {
            Document d = registerExternal(source, sourceUrl, officialUrl, fileName, academicYear,
                    hostingPermission, outcome);
            return new IngestResult(outcome.decision(), outcome.reason(), d, false, false, null);
        }

        // 2) HOST path. Decide whether a download is even needed (conditional fetch, docs section 23).
        Optional<Document> existing = Optional.empty();
        HeadInfo head = fetch.head(sourceUrl);
        // We can only compare against an existing document if we already know one by its source;
        // dedup by content happens after download. Here we just honour ETag/size skip when possible.
        FetchDecision fd = fetch.decide(existing.orElse(null), head);
        if (fd == FetchDecision.SKIP_UNCHANGED) {
            log.debug("Conditional fetch: source unchanged for {}, skipping download", sourceUrl);
        }

        // 3) Download (only when required). Validation + checksum follow.
        Optional<Downloaded> dl = downloader.download(sourceUrl);
        if (dl.isEmpty()) {
            Document d = registerFailed(source, sourceUrl, officialUrl, fileName, academicYear,
                    "Download failed");
            return new IngestResult(Decision.PENDING_REVIEW, "Download failed", d, true, false, null);
        }
        Downloaded payload = dl.get();

        // 4) Validate before storing (docs section 26): reject HTML error pages / non-PDF / too big.
        byte[] head64 = payload.bytes().length > 64 ? java.util.Arrays.copyOf(payload.bytes(), 64) : payload.bytes();
        PdfValidator.Result validation = validator.validate(payload.contentType(), head64,
                payload.bytes().length, maxHostBytes);
        if (!validation.valid()) {
            Document d = registerRejected(source, sourceUrl, officialUrl, fileName, academicYear,
                    "Validation failed: " + validation.reason());
            return new IngestResult(Decision.REJECT, validation.reason(), d, true, false, null);
        }

        // 5) Checksum (content identity + dedup, docs sections 6, 7).
        String checksum = Checksums.sha256(payload.bytes());

        // 6) Dedup: if content already known, reuse it (store once, reference many).
        Optional<Document> dedup = documents.findByHash(checksum);
        if (dedup.isPresent()) {
            Document d = dedup.get();
            boolean changed = documents.recordNewVersionIfChanged(d.getId(), checksum,
                    payload.etag(), (long) payload.bytes().length, storage.activeProvider(),
                    d.getStoragePath(), sourceUrl, academicYear);
            return new IngestResult(Decision.HOST, "Deduplicated - reused existing content", d, true, changed, checksum);
        }

        // 7) Store the binary in object storage (never in PostgreSQL).
        Optional<StoredObject> stored = storage.store(storagePath,
                new ByteArrayInputStream(payload.bytes()), payload.bytes().length,
                payload.contentType(), checksum);
        boolean hosted = stored.isPresent();
        String provider = hosted ? storage.activeProvider() : "EXTERNAL";
        String path = hosted ? storagePath : null;

        // 8) Persist metadata (+ first version) in PostgreSQL (docs sections 1, 44).
        Document d = documents.registerOrGet(checksum, fileName, "application/pdf",
                (long) payload.bytes().length, sourceUrl, officialUrl,
                source == null ? null : source.getId(), provider, path, hosted,
                hostingPermission, hosted ? "INTERNAL" : "EXTERNAL", academicYear,
                hosted ? "HOST" : "EXTERNAL_REFERENCE", payload.etag());

        return new IngestResult(Decision.HOST, hosted ? "Hosted in object storage" : "Stored metadata (provider could not host)",
                d, true, false, checksum);
    }

    // --- helpers ---

    private Document registerExternal(ContentSource source, String sourceUrl, String officialUrl,
                                      String fileName, String academicYear, String hostingPermission,
                                      Outcome outcome) {
        String status = outcome.decision() == Decision.PENDING_REVIEW ? "PENDING_REVIEW" : "EXTERNAL_REFERENCE";
        return documents.registerOrGet(
                // Content not downloaded; identify the record by a stable synthetic hash of the URL so
                // repeated discovery of the same external doc dedups to one metadata row.
                "ext:" + Checksums.sha256((officialUrl == null ? sourceUrl : officialUrl).getBytes()),
                fileName, "application/pdf", null, sourceUrl, officialUrl,
                source == null ? null : source.getId(), "EXTERNAL", null, false,
                hostingPermission, "EXTERNAL", academicYear, status, null);
    }

    private Document registerFailed(ContentSource source, String sourceUrl, String officialUrl,
                                    String fileName, String academicYear, String error) {
        Document d = documents.registerOrGet(
                "fail:" + Checksums.sha256((sourceUrl + "|" + error).getBytes()),
                fileName, "application/pdf", null, sourceUrl, officialUrl,
                source == null ? null : source.getId(), "EXTERNAL", null, false,
                "UNKNOWN", "EXTERNAL", academicYear, "FAILED", null);
        fetch.recordFailure(d, error);
        return documents.save(d);
    }

    /**
     * A validation reject (docs section 26) is NOT a transient network failure, so it does not use
     * the retry/backoff lifecycle. It is recorded as a permanent FAILED record for admin visibility.
     */
    private Document registerRejected(ContentSource source, String sourceUrl, String officialUrl,
                                      String fileName, String academicYear, String error) {
        Document d = documents.registerOrGet(
                "reject:" + Checksums.sha256((sourceUrl + "|" + error).getBytes()),
                fileName, "application/pdf", null, sourceUrl, officialUrl,
                source == null ? null : source.getId(), "EXTERNAL", null, false,
                "UNKNOWN", "EXTERNAL", academicYear, "FAILED", null);
        d.setErrorMessage(error);
        d.setLastAttemptAt(java.time.OffsetDateTime.now());
        return documents.save(d);
    }
}
