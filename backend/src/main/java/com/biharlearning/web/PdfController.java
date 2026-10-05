package com.biharlearning.web;

import com.biharlearning.dto.Dtos.PdfResolutionDto;
import com.biharlearning.service.PdfService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * PDF resolution. The frontend never touches external gov sites directly; it asks us, and we
 * return a decision: a direct storage/CDN URL for hosted docs (browser fetches it itself, no
 * backend proxy) or an external LINK to the official source. We never fabricate a URL.
 *
 * Caching (docs section 15): the resolution response is a small, mostly-stable pointer, so we set
 * a short private max-age. The actual PDF bytes are cached by the browser/CDN at the storage URL,
 * and versioned document URLs are immutable.
 */
@RestController
@RequestMapping("/api/v1/content")
public class PdfController {

    private final PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @GetMapping("/book/{bookId}/pdf")
    public ResponseEntity<ApiResponse<PdfResolutionDto>> bookPdf(@PathVariable String bookId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePrivate())
                .body(ApiResponse.ok(pdfService.resolveBook(bookId)));
    }

    @GetMapping("/chapter/{chapterId}/pdf")
    public ResponseEntity<ApiResponse<PdfResolutionDto>> chapterPdf(@PathVariable String chapterId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePrivate())
                .body(ApiResponse.ok(pdfService.resolveChapter(chapterId)));
    }
}
