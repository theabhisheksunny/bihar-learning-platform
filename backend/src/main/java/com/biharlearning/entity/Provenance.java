package com.biharlearning.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.OffsetDateTime;

/**
 * Embeddable provenance block. Every content-bearing entity carries one so the source of
 * any piece of content is always traceable and visible to students.
 * Unverified fields stay {@code null} - never fabricated (see content-policy.md).
 */
@Embeddable
public class Provenance {

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "source_name")
    private String sourceName;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "source_document_url")
    private String sourceDocumentUrl;

    @Column(name = "retrieved_at")
    private OffsetDateTime retrievedAt;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "academic_year")
    private String academicYear;

    @Column(name = "version", nullable = false)
    private int version = 1;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "license")
    private String license;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING_REVIEW;

    public String getSourceType() { return sourceType; }
    public void setSourceType(String v) { this.sourceType = v; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String v) { this.sourceName = v; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String v) { this.sourceUrl = v; }
    public String getSourceDocumentUrl() { return sourceDocumentUrl; }
    public void setSourceDocumentUrl(String v) { this.sourceDocumentUrl = v; }
    public OffsetDateTime getRetrievedAt() { return retrievedAt; }
    public void setRetrievedAt(OffsetDateTime v) { this.retrievedAt = v; }
    public OffsetDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(OffsetDateTime v) { this.publishedAt = v; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String v) { this.academicYear = v; }
    public int getVersion() { return version; }
    public void setVersion(int v) { this.version = v; }
    public String getChecksum() { return checksum; }
    public void setChecksum(String v) { this.checksum = v; }
    public String getLicense() { return license; }
    public void setLicense(String v) { this.license = v; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus v) { this.verificationStatus = v; }
}
