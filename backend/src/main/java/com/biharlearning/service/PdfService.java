package com.biharlearning.service;

import com.biharlearning.dto.Dtos.PdfResolutionDto;
import com.biharlearning.entity.Book;
import com.biharlearning.entity.Chapter;
import com.biharlearning.entity.Document;
import com.biharlearning.entity.Provenance;
import com.biharlearning.exception.NotFoundException;
import com.biharlearning.repository.*;
import com.biharlearning.storage.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Resolves a book/chapter to a PDF access decision WITHOUT the frontend ever touching an external
 * gov site directly.
 *
 * Resolution strategy (docs sections 12, 35, 44):
 *  1. If a linked {@link Document} is HOSTED by the storage provider, return a direct
 *     storage/CDN URL (browser fetches it directly - no Spring Boot proxy) with accessMode
 *     INTERNAL/STREAM.
 *  2. Otherwise return an EXTERNAL link to the official source (accessMode EXTERNAL).
 *  3. If no verified URL exists at all, report PDF_SOURCE_UNAVAILABLE (we never fabricate a URL).
 */
@Service
public class PdfService {

    private final BookRepository bookRepo;
    private final ChapterRepository chapterRepo;
    private final BookDocumentRepository bookDocs;
    private final ChapterDocumentRepository chapterDocs;
    private final DocumentRepository documents;
    private final FileStorageService storage;
    private final int signedUrlExpiry;

    public PdfService(BookRepository bookRepo, ChapterRepository chapterRepo,
                      BookDocumentRepository bookDocs, ChapterDocumentRepository chapterDocs,
                      DocumentRepository documents, FileStorageService storage,
                      @Value("${app.storage.signed-url-expiry-seconds:3600}") int signedUrlExpiry) {
        this.bookRepo = bookRepo;
        this.chapterRepo = chapterRepo;
        this.bookDocs = bookDocs;
        this.chapterDocs = chapterDocs;
        this.documents = documents;
        this.storage = storage;
        this.signedUrlExpiry = signedUrlExpiry;
    }

    public PdfResolutionDto resolveBook(String bookId) {
        Book b = bookRepo.findBySlug(bookId).orElseGet(() -> {
            try { return bookRepo.findById(UUID.fromString(bookId)).orElse(null); }
            catch (IllegalArgumentException e) { return null; }
        });
        if (b == null) throw new NotFoundException("BOOK_NOT_FOUND", "Book could not be found");

        // Prefer a linked hosted document.
        Optional<Document> linked = firstDocument(bookDocs.findByBookId(b.getId()).stream()
                .map(bd -> bd.getDocumentId()).toList());
        if (linked.isPresent()) {
            return fromDocument(linked.get(), b.getProvenance());
        }
        // Fall back to the official source URL from provenance (external reference).
        String url = firstNonBlank(b.getProvenance().getSourceDocumentUrl(), b.getPdfUrl(),
                b.getOfficialSourceUrl());
        if (url == null) {
            throw new NotFoundException("PDF_SOURCE_UNAVAILABLE",
                    "No verified PDF source is available for this book yet");
        }
        return external(url, b.getProvenance());
    }

    public PdfResolutionDto resolveChapter(String chapterId) {
        Chapter c;
        try {
            c = chapterRepo.findById(UUID.fromString(chapterId))
                    .orElseThrow(() -> new NotFoundException("CHAPTER_NOT_FOUND", "Chapter could not be found"));
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("CHAPTER_NOT_FOUND", "Chapter could not be found");
        }

        // Prefer a document linked to the chapter.
        Optional<Document> chapterDoc = firstDocument(chapterDocs.findByChapterId(c.getId()).stream()
                .map(cd -> cd.getDocumentId()).toList());
        if (chapterDoc.isPresent()) {
            return fromDocument(chapterDoc.get(), c.getProvenance());
        }

        // Then a document linked to the parent book.
        Book b = bookRepo.findById(c.getBookId()).orElse(null);
        if (b != null) {
            Optional<Document> bookDoc = firstDocument(bookDocs.findByBookId(b.getId()).stream()
                    .map(bd -> bd.getDocumentId()).toList());
            if (bookDoc.isPresent()) {
                return fromDocument(bookDoc.get(), b.getProvenance());
            }
        }

        // Finally fall back to a provenance URL (chapter, then book).
        String url = firstNonBlank(c.getProvenance().getSourceDocumentUrl());
        Provenance attribution = c.getProvenance();
        if (url == null && b != null) {
            url = firstNonBlank(b.getProvenance().getSourceDocumentUrl(), b.getPdfUrl(),
                    b.getOfficialSourceUrl());
            if (url != null) attribution = b.getProvenance();
        }
        if (url == null) {
            throw new NotFoundException("PDF_SOURCE_UNAVAILABLE",
                    "No verified PDF source is available for this chapter yet");
        }
        return external(url, attribution);
    }

    // --- helpers ---

    private Optional<Document> firstDocument(List<UUID> ids) {
        for (UUID id : ids) {
            Optional<Document> d = documents.findById(id);
            if (d.isPresent()) return d;
        }
        return Optional.empty();
    }

    /** Build a resolution for a linked Document: direct storage URL if hosted, else external. */
    private PdfResolutionDto fromDocument(Document d, Provenance p) {
        if (d.isHosted() && d.getStoragePath() != null) {
            // Prefer a signed URL so the browser fetches directly from storage/CDN (no backend proxy).
            String url = storage.signedUrl(d.getStoragePath(), signedUrlExpiry)
                    .or(() -> storage.publicUrl(d.getStoragePath()))
                    .orElse(d.getOfficialSourceUrl());
            String mode = url != null && url.equals(d.getOfficialSourceUrl()) ? "LINK" : "STREAM";
            return new PdfResolutionDto(mode, url, p == null ? null : p.getSourceName(),
                    p == null ? null : p.getLicense(),
                    p == null || p.getVerificationStatus() == null ? null : p.getVerificationStatus().name(),
                    "Platform-hosted document served directly from object storage.",
                    d.getAccessMode(), d.getStorageProvider(), d.getVersion(), d.getFileSize());
        }
        // Not hosted: external reference to the official source.
        String url = firstNonBlank(d.getOfficialSourceUrl(), d.getSourceUrl());
        if (url == null) {
            throw new NotFoundException("PDF_SOURCE_UNAVAILABLE",
                    "Document is not hosted and has no official source URL");
        }
        return new PdfResolutionDto("LINK", url, p == null ? null : p.getSourceName(),
                p == null ? null : p.getLicense(),
                p == null || p.getVerificationStatus() == null ? null : p.getVerificationStatus().name(),
                "Opening the official source. Content belongs to its publisher; attribution preserved.",
                "EXTERNAL", d.getStorageProvider(), d.getVersion(), d.getFileSize());
    }

    private PdfResolutionDto external(String url, Provenance p) {
        return new PdfResolutionDto("LINK", url, p == null ? null : p.getSourceName(),
                p == null ? null : p.getLicense(),
                p == null || p.getVerificationStatus() == null ? null : p.getVerificationStatus().name(),
                "Opening the official source. Content belongs to its publisher; attribution preserved.",
                "EXTERNAL", "EXTERNAL", null, null);
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) if (v != null && !v.isBlank()) return v;
        return null;
    }
}
