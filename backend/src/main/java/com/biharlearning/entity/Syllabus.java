package com.biharlearning.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "syllabus")
public class Syllabus {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "class_id", nullable = false)
    private UUID classId;

    @Column(name = "subject_id")
    private UUID subjectId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String body;

    @Column(name = "document_url")
    private String documentUrl;

    @Column(name = "source_type")
    private String sourceType;
    @Column(name = "source_name")
    private String sourceName;
    @Column(name = "source_url")
    private String sourceUrl;
    @Column(name = "academic_year")
    private String academicYear;
    private String license;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING_REVIEW;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public UUID getId() { return id; }
    public UUID getClassId() { return classId; }
    public void setClassId(UUID classId) { this.classId = classId; }
    public UUID getSubjectId() { return subjectId; }
    public void setSubjectId(UUID subjectId) { this.subjectId = subjectId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String v) { this.documentUrl = v; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String v) { this.sourceType = v; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String v) { this.sourceName = v; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String v) { this.sourceUrl = v; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String v) { this.academicYear = v; }
    public String getLicense() { return license; }
    public void setLicense(String v) { this.license = v; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus v) { this.verificationStatus = v; }
}
