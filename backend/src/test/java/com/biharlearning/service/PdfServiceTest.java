package com.biharlearning.service;

import com.biharlearning.dto.Dtos.PdfResolutionDto;
import com.biharlearning.entity.Book;
import com.biharlearning.entity.BookDocument;
import com.biharlearning.entity.Chapter;
import com.biharlearning.entity.Document;
import com.biharlearning.entity.Provenance;
import com.biharlearning.exception.NotFoundException;
import com.biharlearning.repository.BookDocumentRepository;
import com.biharlearning.repository.BookRepository;
import com.biharlearning.repository.ChapterDocumentRepository;
import com.biharlearning.repository.ChapterRepository;
import com.biharlearning.repository.DocumentRepository;
import com.biharlearning.storage.FileStorageService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PDF resolution policy:
 *  - external LINK decision when only an official URL exists,
 *  - fallback to the parent book,
 *  - the honest "unavailable" path when no verified document URL exists (no fabricated URLs),
 *  - a hosted document resolving to a direct storage/CDN STREAM URL (docs sections 12, 35).
 */
class PdfServiceTest {

    private final BookRepository bookRepo = mock(BookRepository.class);
    private final ChapterRepository chapterRepo = mock(ChapterRepository.class);
    private final BookDocumentRepository bookDocs = mock(BookDocumentRepository.class);
    private final ChapterDocumentRepository chapterDocs = mock(ChapterDocumentRepository.class);
    private final DocumentRepository documents = mock(DocumentRepository.class);
    private final FileStorageService storage = mock(FileStorageService.class);

    private final PdfService service = new PdfService(
            bookRepo, chapterRepo, bookDocs, chapterDocs, documents, storage, 3600);

    @Test
    void resolvesBookToLinkWhenDocumentUrlPresent() {
        Book b = new Book();
        b.setSlug("bihar-class-9-mathematics");
        Provenance p = new Provenance();
        p.setSourceDocumentUrl("https://example.gov/doc.pdf");
        p.setSourceName("NCERT");
        b.setProvenance(p);
        when(bookRepo.findBySlug("bihar-class-9-mathematics")).thenReturn(Optional.of(b));
        when(bookDocs.findByBookId(any())).thenReturn(List.of());

        PdfResolutionDto res = service.resolveBook("bihar-class-9-mathematics");
        assertEquals("LINK", res.mode());
        assertEquals("https://example.gov/doc.pdf", res.url());
        assertEquals("NCERT", res.sourceName());
    }

    @Test
    void throwsWhenNoVerifiedPdf() {
        Book b = new Book();
        b.setSlug("no-pdf");
        b.setProvenance(new Provenance()); // all null
        when(bookRepo.findBySlug("no-pdf")).thenReturn(Optional.of(b));
        when(bookDocs.findByBookId(any())).thenReturn(List.of());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.resolveBook("no-pdf"));
        assertEquals("PDF_SOURCE_UNAVAILABLE", ex.getCode());
    }

    @Test
    void chapterFallsBackToParentBookSource() {
        UUID chapterId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        Chapter ch = new Chapter();
        ch.setBookId(bookId);
        ch.setProvenance(new Provenance()); // chapter has no doc url
        when(chapterRepo.findById(chapterId)).thenReturn(Optional.of(ch));
        when(chapterDocs.findByChapterId(any())).thenReturn(List.of());
        when(bookDocs.findByBookId(any())).thenReturn(List.of());

        Book b = new Book();
        Provenance bp = new Provenance();
        bp.setSourceDocumentUrl("https://example.gov/book.pdf");
        bp.setSourceName("BEP e-LOTS");
        b.setProvenance(bp);
        when(bookRepo.findById(bookId)).thenReturn(Optional.of(b));

        PdfResolutionDto res = service.resolveChapter(chapterId.toString());
        assertEquals("LINK", res.mode());
        assertEquals("https://example.gov/book.pdf", res.url());
        assertEquals("BEP e-LOTS", res.sourceName());
    }

    @Test
    void resolvesHostedDocumentToDirectStorageStreamUrl() {
        Book b = new Book();
        b.setSlug("hosted-book");
        b.setProvenance(new Provenance());
        when(bookRepo.findBySlug("hosted-book")).thenReturn(Optional.of(b));

        UUID docId = UUID.randomUUID();
        BookDocument bd = new BookDocument();
        bd.setBookId(UUID.randomUUID());
        bd.setDocumentId(docId);
        bd.setRole("TEXTBOOK");
        when(bookDocs.findByBookId(any())).thenReturn(List.of(bd));

        Document d = new Document();
        d.setId(docId);
        d.setHosted(true);
        d.setStoragePath("documents/class-09/mathematics/2026-27/textbook/math.pdf");
        d.setAccessMode("INTERNAL");
        d.setStorageProvider("SUPABASE");
        d.setVersion(1);
        d.setFileSize(1234L);
        when(documents.findById(docId)).thenReturn(Optional.of(d));

        when(storage.signedUrl(anyString(), anyInt()))
                .thenReturn(Optional.of("https://cdn.supabase.co/signed/math.pdf"));

        PdfResolutionDto res = service.resolveBook("hosted-book");
        assertEquals("STREAM", res.mode());
        assertEquals("https://cdn.supabase.co/signed/math.pdf", res.url());
        assertEquals("INTERNAL", res.accessMode());
        assertEquals("SUPABASE", res.storageProvider());
    }
}
