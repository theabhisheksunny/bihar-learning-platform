package com.biharlearning.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/** History of a document's content. A new row is created only when content genuinely changes. */
@Entity
@Table(name = "document_versions")
public class DocumentVersion {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(nullable = false)
    private int version;

    @Column(name = "document_hash", nullable = false)
    private String documentHash;

    private String etag;
    @Column(name = "last_modified")
    private String lastModified;
    @Column(name = "file_size")
    private Long fileSize;
    @Column(name = "storage_provider")
    private String storageProvider;
    @Column(name = "storage_path")
    private String storagePath;
    @Column(name = "source_url")
    private String sourceUrl;
    @Column(name = "academic_year")
    private String academicYear;

    @Column(nullable = false)
    private String retention = "CURRENT"; // CURRENT, PREVIOUS, ARCHIVED

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public UUID getId() { return id; }
    public UUID getDocumentId() { return documentId; }
    public void setDocumentId(UUID v) { this.documentId = v; }
    public int getVersion() { return version; }
    public void setVersion(int v) { this.version = v; }
    public String getDocumentHash() { return documentHash; }
    public void setDocumentHash(String v) { this.documentHash = v; }
    public String getEtag() { return etag; }
    public void setEtag(String v) { this.etag = v; }
    public String getLastModified() { return lastModified; }
    public void setLastModified(String v) { this.lastModified = v; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long v) { this.fileSize = v; }
    public String getStorageProvider() { return storageProvider; }
    public void setStorageProvider(String v) { this.storageProvider = v; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String v) { this.storagePath = v; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String v) { this.sourceUrl = v; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String v) { this.academicYear = v; }
    public String getRetention() { return retention; }
    public void setRetention(String v) { this.retention = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
