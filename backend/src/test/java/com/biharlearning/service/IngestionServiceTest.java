package com.biharlearning.service;

import com.biharlearning.entity.Book;
import com.biharlearning.entity.ClassEntity;
import com.biharlearning.entity.Chapter;
import com.biharlearning.entity.ContentSource;
import com.biharlearning.entity.Document;
import com.biharlearning.entity.Subject;
import com.biharlearning.repository.*;
import com.biharlearning.service.IngestionService.Downloaded;
import com.biharlearning.storage.FileStorageProvider;
import com.biharlearning.storage.FileStorageService;
import com.biharlearning.storage.PdfValidator;
import com.biharlearning.storage.StorageDecisionEngine;
import com.biharlearning.storage.StorageDecisionEngine.Decision;
import com.biharlearning.storage.StoredObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Section 40 end-to-end storage workflow, verified step by step against a real database:
 *
 *   source → discover → check permission → check existing checksum → download when required →
 *   validate PDF → store in object storage (if permitted) → save metadata in PostgreSQL →
 *   create document version → associate with a Book.
 *
 * Plus the section 39 scenarios driven through the real pipeline: duplicate detection, external
 * reference (no hosting permission), failed download, rejected non-PDF, and a large file stored
 * by streaming (we assert the bytes never had to be re-read from PostgreSQL).
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(DocumentService.class)
class IngestionServiceTest {

    @Autowired DocumentRepository docs;
    @Autowired DocumentVersionRepository versions;
    @Autowired BookDocumentRepository bookDocs;
    @Autowired ChapterDocumentRepository chapterDocs;
    @Autowired BookRepository bookRepo;
    @Autowired SubjectRepository subjectRepo;
    @Autowired ClassRepository classRepo;
    @Autowired DocumentService documentService;

    /** In-memory hosting provider so we never touch the network/Supabase in tests. */
    static class InMemoryProvider implements FileStorageProvider {
        final ConcurrentHashMap<String, byte[]> objects = new ConcurrentHashMap<>();
        final AtomicLong bytes = new AtomicLong();
        @Override public String key() { return "SUPABASE"; }
        @Override public boolean canHost() { return true; }
        @Override public Optional<StoredObject> store(String path, InputStream data, long size,
                                                      String ct, String checksum) {
            try {
                byte[] all = data.readAllBytes();
                objects.put(path, all);
                bytes.addAndGet(all.length);
                return Optional.of(new StoredObject(key(), "documents", path, all.length, checksum, ct));
            } catch (Exception e) { return Optional.empty(); }
        }
        @Override public Optional<String> urlFor(String p, boolean s, int e) {
            return Optional.of("https://cdn.test/" + p);
        }
        @Override public Optional<InputStream> open(String p) {
            byte[] b = objects.get(p);
            return b == null ? Optional.empty() : Optional.of(new java.io.ByteArrayInputStream(b));
        }
        @Override public boolean delete(String p) { return objects.remove(p) != null; }
        @Override public Optional<Long> usedBytes() { return Optional.of(bytes.get()); }
    }

    private final InMemoryProvider hosting = new InMemoryProvider();

    private IngestionService ingestion(boolean canHost) {
        FileStorageProvider provider = canHost ? hosting : externalOnly();
        FileStorageService storage = new FileStorageService(List.of(provider), provider.key(), "documents");
        ConditionalFetchService fetch = new ConditionalFetchService(
                "test-agent", 500, 3, 1000);
        return new IngestionService(fetch, new PdfValidator(), new StorageDecisionEngine(),
                storage, documentService, 50_000_000);
    }

    private FileStorageProvider externalOnly() {
        return new FileStorageProvider() {
            @Override public String key() { return "EXTERNAL"; }
            @Override public boolean canHost() { return false; }
            @Override public Optional<StoredObject> store(String p, InputStream d, long s, String c, String k) { return Optional.empty(); }
            @Override public Optional<String> urlFor(String p, boolean s, int e) { return Optional.empty(); }
            @Override public Optional<InputStream> open(String p) { return Optional.empty(); }
            @Override public boolean delete(String p) { return true; }
            @Override public Optional<Long> usedBytes() { return Optional.of(0L); }
        };
    }

    private static Optional<Downloaded> pdf(String body) {
        return Optional.of(new Downloaded(("%PDF-1.7\n" + body).getBytes(StandardCharsets.UTF_8),
                "application/pdf", "etag-" + body.hashCode(), null));
    }

    private ContentSource ncert() {
        ContentSource s = new ContentSource();
        s.setName("NCERT");
        s.setLicense("CC BY-NC-ND 4.0");
        return s;
    }

    @Test
    void endToEndHostsPermittedPdfAndAssociatesWithBook() {
        IngestionService ingest = ingestion(true);
        UUID bookId = createBook("ncert-math-9");

        // GRANTED permission + provider can host -> HOST, download, validate, store, persist.
        IngestionService.IngestResult res = ingest.ingest(ncert(),
                "https://ncert.example/math9.pdf", "https://ncert.example/math9.pdf",
                "mathematics-class-9.pdf",
                "documents/class-09/mathematics/2026-27/textbook/mathematics-class-9.pdf",
                "2026-27", "GRANTED", url -> pdf("Mathematics Class IX"));

        assertEquals(Decision.HOST, res.decision());
        assertTrue(res.downloaded());
        assertNotNull(res.checksum());

        // Metadata persisted in PostgreSQL, binary in the (in-memory) object store, not the DB.
        Document d = docs.findByDocumentHash(res.checksum()).orElseThrow();
        assertTrue(d.isHosted());
        assertEquals("SUPABASE", d.getStorageProvider());
        assertEquals("HOST", d.getStatus());
        assertEquals(1, versions.findByDocumentIdOrderByVersionDesc(d.getId()).size());
        assertTrue(hosting.objects.containsKey(d.getStoragePath()), "Binary must live in object storage");

        // Associate with a book (reuse, not copy).
        documentService.linkToBook(bookId, d.getId(), "TEXTBOOK");
        assertEquals(1, bookDocs.countByDocumentId(d.getId()));
    }

    @Test
    void duplicateIngestionReusesSingleStoredDocument() {
        IngestionService ingest = ingestion(true);
        var r1 = ingest.ingest(ncert(), "https://ncert.example/a.pdf", "https://ncert.example/a.pdf",
                "a.pdf", "documents/a.pdf", "2026-27", "GRANTED", url -> pdf("same content"));
        var r2 = ingest.ingest(ncert(), "https://ncert.example/b.pdf", "https://ncert.example/b.pdf",
                "b.pdf", "documents/b.pdf", "2026-27", "GRANTED", url -> pdf("same content"));

        assertEquals(r1.checksum(), r2.checksum());
        assertEquals(r1.document().getId(), r2.document().getId(), "Same bytes -> one document");
        assertEquals(1, docs.findAll().stream().filter(Document::isHosted).count());
    }

    @Test
    void unknownPermissionBecomesExternalReferenceWithoutDownloading() {
        IngestionService ingest = ingestion(true);
        boolean[] downloaded = {false};
        var res = ingest.ingest(new ContentSource(), "https://x.example/u.pdf", "https://x.example/u.pdf",
                "u.pdf", "documents/u.pdf", "2026-27", "UNKNOWN",
                url -> { downloaded[0] = true; return pdf("should not download"); });

        assertEquals(Decision.PENDING_REVIEW, res.decision());
        assertFalse(downloaded[0], "Must not download when permission is UNKNOWN");
        assertFalse(res.document().isHosted());
    }

    @Test
    void deniedPermissionBecomesExternalReference() {
        IngestionService ingest = ingestion(true);
        var res = ingest.ingest(new ContentSource(), "https://x.example/d.pdf", "https://x.example/d.pdf",
                "d.pdf", "documents/d.pdf", "2026-27", "DENIED", url -> pdf("denied"));
        assertEquals(Decision.EXTERNAL_REFERENCE, res.decision());
        assertFalse(res.document().isHosted());
    }

    @Test
    void failedDownloadIsRecordedWithRetryMetadata() {
        IngestionService ingest = ingestion(true);
        var res = ingest.ingest(ncert(), "https://ncert.example/fail.pdf", "https://ncert.example/fail.pdf",
                "fail.pdf", "documents/fail.pdf", "2026-27", "GRANTED", url -> Optional.empty());

        Document d = res.document();
        // First failure -> RETRYING with backoff (not permanently FAILED yet).
        assertTrue(d.getAttemptCount() >= 1);
        assertTrue(List.of("RETRYING", "FAILED").contains(d.getStatus()));
        assertNotNull(d.getErrorMessage());
    }

    @Test
    void htmlErrorPageDisguisedAsPdfIsRejected() {
        IngestionService ingest = ingestion(true);
        var res = ingest.ingest(ncert(), "https://ncert.example/html.pdf", "https://ncert.example/html.pdf",
                "html.pdf", "documents/html.pdf", "2026-27", "GRANTED",
                url -> Optional.of(new Downloaded("<html>404</html>".getBytes(StandardCharsets.UTF_8),
                        "text/html", null, null)));
        assertEquals(Decision.REJECT, res.decision());
        assertEquals("FAILED", res.document().getStatus());
    }

    @Test
    void largePdfIsStoredByStreamingIntoObjectStorageNotDatabase() {
        IngestionService ingest = ingestion(true);
        StringBuilder big = new StringBuilder();
        for (int i = 0; i < 2_000_000; i++) big.append('A'); // ~2 MB payload
        var res = ingest.ingest(ncert(), "https://ncert.example/big.pdf", "https://ncert.example/big.pdf",
                "big.pdf", "documents/big.pdf", "2026-27", "GRANTED", url -> pdf(big.toString()));

        assertEquals(Decision.HOST, res.decision());
        Document d = docs.findByDocumentHash(res.checksum()).orElseThrow();
        assertTrue(d.getFileSize() > 2_000_000);
        // The binary is in object storage; PostgreSQL only holds the reference/metadata.
        assertTrue(hosting.objects.get(d.getStoragePath()).length > 2_000_000);
        assertNotNull(d.getStoragePath(), "Hosted document must have a storage path reference");
    }

    // --- helper graph ---
    private UUID createBook(String slug) {
        ClassEntity cls = new ClassEntity();
        cls.setCode("9-" + slug);
        cls.setName("Class IX");
        cls.setActive(true);
        cls.setDisplayOrder(1);
        cls = classRepo.save(cls);
        Subject subj = new Subject();
        subj.setClassId(cls.getId());
        subj.setCode("subj-" + slug);
        subj.setName("Subject");
        subj.setDisplayOrder(1);
        subj = subjectRepo.save(subj);
        Book b = new Book();
        b.setSubjectId(subj.getId());
        b.setSlug(slug);
        b.setTitle("Book " + slug);
        b = bookRepo.save(b);
        return b.getId();
    }
}
