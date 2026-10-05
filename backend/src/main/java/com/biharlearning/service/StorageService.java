package com.biharlearning.service;

import com.biharlearning.dto.Dtos.DocumentDto;
import com.biharlearning.dto.Dtos.StorageStatsDto;
import com.biharlearning.entity.Document;
import com.biharlearning.entity.DocumentVersion;
import com.biharlearning.repository.*;
import com.biharlearning.storage.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Storage monitoring, quotas and cleanup candidate reporting (docs sections 19-21, 42).
 *
 * Honesty rule: usage is computed from OUR documents/images tables (bytes we actually track).
 * The total quota is reported as "UNKNOWN" unless an operator has configured STORAGE_QUOTA_BYTES
 * - we never invent a provider quota.
 */
@Service
public class StorageService {

    private final DocumentRepository docs;
    private final DocumentVersionRepository versions;
    private final ImageAssetRepository images;
    private final BookDocumentRepository bookDocs;
    private final ChapterDocumentRepository chapterDocs;
    private final FileStorageService storage;
    private final DocumentService documentService;

    private final long quotaBytes;
    private final int warnPercent;
    private final int criticalPercent;

    public StorageService(DocumentRepository docs, DocumentVersionRepository versions,
                          ImageAssetRepository images, BookDocumentRepository bookDocs,
                          ChapterDocumentRepository chapterDocs, FileStorageService storage,
                          DocumentService documentService,
                          @Value("${app.storage.quota-bytes:0}") long quotaBytes,
                          @Value("${app.storage.warn-threshold-percent:70}") int warnPercent,
                          @Value("${app.storage.critical-threshold-percent:85}") int criticalPercent) {
        this.docs = docs;
        this.versions = versions;
        this.images = images;
        this.bookDocs = bookDocs;
        this.chapterDocs = chapterDocs;
        this.storage = storage;
        this.documentService = documentService;
        this.quotaBytes = quotaBytes;
        this.warnPercent = warnPercent;
        this.criticalPercent = criticalPercent;
    }

    public StorageStatsDto stats() {
        long pdfCount = docs.countByHosted(true);
        long pdfBytes = docs.sumHostedBytes();
        long imageCount = images.countByHosted(true);
        long imageBytes = images.sumHostedBytes();
        long externalDocs = docs.countByHosted(false);
        long failed = docs.countByStatus("FAILED");
        long candidates = docs.findByCandidateForDeletionTrue().size();

        long currentBytes = 0, archivedBytes = 0;
        for (DocumentVersion v : versions.findAll()) {
            long size = v.getFileSize() == null ? 0 : v.getFileSize();
            if ("ARCHIVED".equals(v.getRetention())) archivedBytes += size;
            else if ("CURRENT".equals(v.getRetention())) currentBytes += size;
        }

        long totalFiles = pdfCount + imageCount;
        long totalBytes = pdfBytes + imageBytes;

        // Quota + usage: UNKNOWN unless configured, and prefer the provider's own figure if given.
        String quotaStr;
        String usagePercentStr;
        String level;
        long effectiveQuota = storage.providerUsedBytes().isPresent() && quotaBytes <= 0
                ? 0 : quotaBytes;
        if (effectiveQuota > 0) {
            double pct = (totalBytes * 100.0) / effectiveQuota;
            quotaStr = String.valueOf(effectiveQuota);
            usagePercentStr = String.format("%.1f", pct);
            level = pct >= criticalPercent ? "CRITICAL" : pct >= warnPercent ? "WARNING" : "OK";
        } else {
            quotaStr = "UNKNOWN";
            usagePercentStr = "UNKNOWN";
            level = "UNKNOWN";
        }

        return new StorageStatsDto(totalFiles, totalBytes, pdfCount, pdfBytes, imageCount, imageBytes,
                currentBytes, archivedBytes, externalDocs, quotaStr, usagePercentStr, level,
                candidates, failed, storage.activeProvider());
    }

    public List<DocumentDto> listDocuments() {
        return docs.findAll().stream().map(this::toDto).toList();
    }

    public List<DocumentDto> duplicatesCandidates() {
        // By construction duplicates cannot exist (unique hash). Return hosted docs for inspection.
        return docs.findByHosted(true).stream().map(this::toDto).toList();
    }

    public List<DocumentDto> orphans() {
        documentService.flagOrphans();
        return docs.findByCandidateForDeletionTrue().stream().map(this::toDto).toList();
    }

    public List<DocumentDto> failed() {
        return docs.findByStatus("FAILED").stream().map(this::toDto).toList();
    }

    private DocumentDto toDto(Document d) {
        return new DocumentDto(d.getId().toString(), d.getDocumentHash(), d.getFileName(),
                d.getMimeType(), d.getFileSize(), d.getStorageProvider(), d.isHosted(),
                d.getHostingPermission(), d.getAccessMode(), d.getVersion(), d.getStatus(),
                d.isCandidateForDeletion(), d.getSourceUrl(), d.getOfficialSourceUrl());
    }
}
