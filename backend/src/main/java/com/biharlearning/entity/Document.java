package com.biharlearning.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Canonical file metadata. The binary is NEVER stored here (docs section 1, 44) - only
 * references, checksum, version, storage location and permission. Deduplicated by
 * {@code documentHash} (SHA-256) via a unique index.
 */
@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "document_hash", nullable = false, unique = true)
    private String documentHash;

    @Column(name = "file_name")
    private String fileName;
    @Column(name = "original_file_name")
    private String originalFileName;
    @Column(name = "mime_type")
    private String mimeType;
    @Column(name = "file_size")
    private Long fileSize;
    private String etag;

    @Column(name = "source_url")
    private String sourceUrl;
    @Column(name = "official_source_url")
    private String officialSourceUrl;
    @Column(name = "source_id")
    private UUID sourceId;

    @Column(name = "storage_provider", nullable = false)
    private String storageProvider = "EXTERNAL";
    @Column(name = "storage_path")
    private String storagePath;
    @Column(name = "is_hosted", nullable = false)
    private boolean hosted;
    @Column(name = "hosting_permission", nullable = false)
    private String hostingPermission = "UNKNOWN";   // GRANTED, DENIED, UNKNOWN
    @Column(name = "access_mode", nullable = false)
    private String accessMode = "EXTERNAL";          // INTERNAL, EXTERNAL, EMBEDDED, DOWNLOAD, STREAM

    @Column(name = "academic_year")
    private String academicYear;
    private String language;
    private String medium;

    @Column(nullable = false)
    private int version = 1;

    @Column(nullable = false)
    private String status = "PENDING_REVIEW";        // HOST, EXTERNAL_REFERENCE, REJECT, PENDING_REVIEW, FAILED

    @Column(name = "candidate_for_deletion", nullable = false)
    private boolean candidateForDeletion;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;
    @Column(name = "last_attempt_at")
    private OffsetDateTime lastAttemptAt;
    @Column(name = "next_retry_at")
    private OffsetDateTime nextRetryAt;
    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();
    @Column(name = "last_verified_at")
    private OffsetDateTime lastVerifiedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getDocumentHash() { return documentHash; }
    public void setDocumentHash(String v) { this.documentHash = v; }
    public String getFileName() { return fileName; }
    public void setFileName(String v) { this.fileName = v; }
    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String v) { this.originalFileName = v; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String v) { this.mimeType = v; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long v) { this.fileSize = v; }
    public String getEtag() { return etag; }
    public void setEtag(String v) { this.etag = v; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String v) { this.sourceUrl = v; }
    public String getOfficialSourceUrl() { return officialSourceUrl; }
    public void setOfficialSourceUrl(String v) { this.officialSourceUrl = v; }
    public UUID getSourceId() { return sourceId; }
    public void setSourceId(UUID v) { this.sourceId = v; }
    public String getStorageProvider() { return storageProvider; }
    public void setStorageProvider(String v) { this.storageProvider = v; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String v) { this.storagePath = v; }
    public boolean isHosted() { return hosted; }
    public void setHosted(boolean v) { this.hosted = v; }
    public String getHostingPermission() { return hostingPermission; }
    public void setHostingPermission(String v) { this.hostingPermission = v; }
    public String getAccessMode() { return accessMode; }
    public void setAccessMode(String v) { this.accessMode = v; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String v) { this.academicYear = v; }
    public String getLanguage() { return language; }
    public void setLanguage(String v) { this.language = v; }
    public String getMedium() { return medium; }
    public void setMedium(String v) { this.medium = v; }
    public int getVersion() { return version; }
    public void setVersion(int v) { this.version = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public boolean isCandidateForDeletion() { return candidateForDeletion; }
    public void setCandidateForDeletion(boolean v) { this.candidateForDeletion = v; }
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int v) { this.attemptCount = v; }
    public OffsetDateTime getLastAttemptAt() { return lastAttemptAt; }
    public void setLastAttemptAt(OffsetDateTime v) { this.lastAttemptAt = v; }
    public OffsetDateTime getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(OffsetDateTime v) { this.nextRetryAt = v; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String v) { this.errorMessage = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime v) { this.updatedAt = v; }
    public OffsetDateTime getLastVerifiedAt() { return lastVerifiedAt; }
    public void setLastVerifiedAt(OffsetDateTime v) { this.lastVerifiedAt = v; }
}
