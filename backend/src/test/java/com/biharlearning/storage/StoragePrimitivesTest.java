package com.biharlearning.storage;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Section 39 primitives: content hashing (dedup identity), PDF validation (reject HTML-as-PDF),
 * the storage decision engine (legal safety default), and streaming a large file without buffering
 * it entirely in memory.
 */
class StoragePrimitivesTest {

    // --- Dedup identity: same bytes -> same SHA-256; different bytes -> different hash (docs 6,7) ---

    @Test
    void identicalContentProducesIdenticalChecksum() {
        byte[] a = "%PDF-1.7 identical content".getBytes(StandardCharsets.UTF_8);
        byte[] b = "%PDF-1.7 identical content".getBytes(StandardCharsets.UTF_8);
        assertEquals(Checksums.sha256(a), Checksums.sha256(b),
                "Two files with identical bytes must hash the same (deduplicate to one document)");
    }

    @Test
    void changedContentProducesDifferentChecksum() {
        String h2025 = Checksums.sha256("%PDF-1.7 Mathematics Class IX 2025".getBytes(StandardCharsets.UTF_8));
        String h2026 = Checksums.sha256("%PDF-1.7 Mathematics Class IX 2026".getBytes(StandardCharsets.UTF_8));
        assertNotEquals(h2025, h2026, "Changed content must hash differently (triggers a new version)");
    }

    // --- Large file: stream through the digest without loading it all (docs 11) ---

    @Test
    void streamingChecksumMatchesInMemoryForLargeInput() throws IOException {
        byte[] big = new byte[8 * 1024 * 1024]; // 8 MB
        for (int i = 0; i < big.length; i++) big[i] = (byte) (i % 251);
        String inMemory = Checksums.sha256(big);

        // A stream that never exposes the whole array to the caller at once.
        try (InputStream in = new ByteArrayInputStream(big)) {
            String streamed = Checksums.sha256Streaming(in);
            assertEquals(inMemory, streamed, "Streaming digest must equal the whole-array digest");
        }
    }

    // --- PDF validation (docs 26) ---

    @Test
    void rejectsHtmlErrorPageDisguisedAsPdf() {
        PdfValidator v = new PdfValidator();
        byte[] html = "<html><body>404 Not Found</body></html>".getBytes(StandardCharsets.UTF_8);
        PdfValidator.Result r = v.validate("text/html", html, html.length, 50_000_000);
        assertFalse(r.valid());
    }

    @Test
    void acceptsRealPdfSignature() {
        PdfValidator v = new PdfValidator();
        byte[] pdf = "%PDF-1.7\n...".getBytes(StandardCharsets.UTF_8);
        PdfValidator.Result r = v.validate("application/pdf", pdf, pdf.length, 50_000_000);
        assertTrue(r.valid());
    }

    @Test
    void rejectsFileExceedingMaxHostSize() {
        PdfValidator v = new PdfValidator();
        byte[] pdf = "%PDF-1.7 big".getBytes(StandardCharsets.UTF_8);
        PdfValidator.Result r = v.validate("application/pdf", pdf, 100_000_000, 50_000_000);
        assertFalse(r.valid());
    }

    // --- Decision engine (docs 5, 36): default is conservative, never auto-mirror on UNKNOWN ---

    @Test
    void unknownPermissionIsHeldForReviewNotHosted() {
        StorageDecisionEngine e = new StorageDecisionEngine();
        StorageDecisionEngine.Outcome o = e.decide(null, true, false, "UNKNOWN");
        assertEquals(StorageDecisionEngine.Decision.PENDING_REVIEW, o.decision());
    }

    @Test
    void deniedPermissionBecomesExternalReference() {
        StorageDecisionEngine e = new StorageDecisionEngine();
        StorageDecisionEngine.Outcome o = e.decide(null, true, true, "DENIED");
        assertEquals(StorageDecisionEngine.Decision.EXTERNAL_REFERENCE, o.decision());
    }

    @Test
    void grantedPermissionWithHostingProviderHosts() {
        StorageDecisionEngine e = new StorageDecisionEngine();
        StorageDecisionEngine.Outcome o = e.decide(null, true, true, "GRANTED");
        assertEquals(StorageDecisionEngine.Decision.HOST, o.decision());
    }

    @Test
    void providerCannotHostFallsBackToExternalEvenIfGranted() {
        StorageDecisionEngine e = new StorageDecisionEngine();
        StorageDecisionEngine.Outcome o = e.decide(null, false, true, "GRANTED");
        assertEquals(StorageDecisionEngine.Decision.EXTERNAL_REFERENCE, o.decision());
    }
}
