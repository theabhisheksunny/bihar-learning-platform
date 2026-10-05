package com.biharlearning.service;

import com.biharlearning.entity.Document;
import com.biharlearning.entity.DocumentVersion;
import com.biharlearning.repository.BookDocumentRepository;
import com.biharlearning.repository.ChapterDocumentRepository;
import com.biharlearning.repository.DocumentRepository;
import com.biharlearning.repository.DocumentVersionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Core deduplication + versioning logic (docs sections 6, 7, 8, 9).
 *
 *  - Dedup: a document is identified by its SHA-256 ({@code documentHash}). Registering the same
 *    content twice returns the existing document; callers add a reference instead of a copy.
 *  - Versioning: a NEW version is created only when the content genuinely changes (checksum
 *    differs) - not because a URL changed. The previous CURRENT version is demoted to PREVIOUS.
 */
@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository docs;
    private final DocumentVersionRepository versions;
    private final BookDocumentRepository bookDocs;
    private final ChapterDocumentRepository chapterDocs;

    public DocumentService(DocumentRepository docs, DocumentVersionRepository versions,
                           BookDocumentRepository bookDocs, ChapterDocumentRepository chapterDocs) {
        this.docs = docs;
        this.versions = versions;
        this.bookDocs = bookDocs;
        this.chapterDocs = chapterDocs;
    }

    public Optional<Document> findByHash(String hash) {
        return docs.findByDocumentHash(hash);
    }

    /**
     * Register content by checksum. If a document with this hash already exists, it is returned
     * unchanged (deduplication). Otherwise a new document + its first version row are created.
     */
    @Transactional
    public Document registerOrGet(String documentHash, String fileName, String mimeType,
                                  Long fileSize, String sourceUrl, String officialSourceUrl,
                                  UUID sourceId, String storageProvider, String storagePath,
                                  boolean hosted, String hostingPermission, String accessMode,
                                  String academicYear, String status, String etag) {
        Optional<Document> existing = docs.findByDocumentHash(documentHash);
        if (existing.isPresent()) {
            log.debug("Dedup hit for {} - reusing document {}", documentHash, existing.get().getId());
            return existing.get();
        }
        Document d = new Document();
        d.setDocumentHash(documentHash);
        d.setFileName(fileName);
        d.setOriginalFileName(fileName);
        d.setMimeType(mimeType);
        d.setFileSize(fileSize);
        d.setSourceUrl(sourceUrl);
        d.setOfficialSourceUrl(officialSourceUrl);
        d.setSourceId(sourceId);
        d.setStorageProvider(storageProvider);
        d.setStoragePath(storagePath);
        d.setHosted(hosted);
        d.setHostingPermission(hostingPermission);
        d.setAccessMode(accessMode);
        d.setAcademicYear(academicYear);
        d.setStatus(status);
        d.setEtag(etag);
        d.setVersion(1);
        d.setLastVerifiedAt(OffsetDateTime.now());
        d = docs.save(d);

        DocumentVersion v = new DocumentVersion();
        v.setDocumentId(d.getId());
        v.setVersion(1);
        v.setDocumentHash(documentHash);
        v.setEtag(etag);
        v.setFileSize(fileSize);
        v.setStorageProvider(storageProvider);
        v.setStoragePath(storagePath);
        v.setSourceUrl(sourceUrl);
        v.setAcademicYear(academicYear);
        v.setRetention("CURRENT");
        versions.save(v);
        return d;
    }

    /**
     * Record that a document's content changed to a new checksum. Creates a new version, demotes
     * the current one to PREVIOUS, and bumps the document's version pointer. If the hash is the
     * SAME as current, this is a no-op (docs section 8).
     *
     * @return true if a new version was created, false if content was identical.
     */
    @Transactional
    public boolean recordNewVersionIfChanged(UUID documentId, String newHash, String newEtag,
                                             Long newSize, String storageProvider, String storagePath,
                                             String sourceUrl, String academicYear) {
        Document d = docs.findById(documentId).orElseThrow();
        if (d.getDocumentHash().equals(newHash)) {
            d.setLastVerifiedAt(OffsetDateTime.now());
            docs.save(d);
            return false; // identical content -> no new file/version
        }
        // Demote existing CURRENT version(s) to PREVIOUS.
        versions.findByDocumentIdOrderByVersionDesc(documentId).stream()
                .filter(v -> "CURRENT".equals(v.getRetention()))
                .forEach(v -> { v.setRetention("PREVIOUS"); versions.save(v); });

        int next = d.getVersion() + 1;
        DocumentVersion v = new DocumentVersion();
        v.setDocumentId(documentId);
        v.setVersion(next);
        v.setDocumentHash(newHash);
        v.setEtag(newEtag);
        v.setFileSize(newSize);
        v.setStorageProvider(storageProvider);
        v.setStoragePath(storagePath);
        v.setSourceUrl(sourceUrl);
        v.setAcademicYear(academicYear);
        v.setRetention("CURRENT");
        versions.save(v);

        d.setVersion(next);
        d.setDocumentHash(newHash);
        d.setEtag(newEtag);
        d.setFileSize(newSize);
        d.setStorageProvider(storageProvider);
        d.setStoragePath(storagePath);
        d.setAcademicYear(academicYear);
        d.setUpdatedAt(OffsetDateTime.now());
        d.setLastVerifiedAt(OffsetDateTime.now());
        docs.save(d);
        return true;
    }

    /** Link a document to a book (reuse, not copy). */
    @Transactional
    public void linkToBook(UUID bookId, UUID documentId, String role) {
        boolean exists = bookDocs.findByBookId(bookId).stream()
                .anyMatch(bd -> bd.getDocumentId().equals(documentId) && bd.getRole().equals(role));
        if (exists) return;
        com.biharlearning.entity.BookDocument bd = new com.biharlearning.entity.BookDocument();
        bd.setBookId(bookId);
        bd.setDocumentId(documentId);
        bd.setRole(role);
        bookDocs.save(bd);
    }

    /** Persist mutations to a managed/detached document (e.g. after applying failure/retry state). */
    @Transactional
    public Document save(Document d) {
        return docs.save(d);
    }

    /** Mark documents that are no longer referenced by any book/chapter as deletion candidates. */
    @Transactional
    public int flagOrphans() {
        int flagged = 0;
        for (Document d : docs.findAll()) {
            long refs = bookDocs.countByDocumentId(d.getId()) + chapterDocs.countByDocumentId(d.getId());
            boolean orphan = refs == 0;
            if (orphan != d.isCandidateForDeletion()) {
                d.setCandidateForDeletion(orphan);
                docs.save(d);
                if (orphan) flagged++;
            }
        }
        return flagged;
    }
}
