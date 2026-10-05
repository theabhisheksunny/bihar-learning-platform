package com.biharlearning.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "chapter_id", nullable = false)
    private UUID chapterId;

    @Column(nullable = false)
    private String category; // MCQ, VERY_SHORT, SHORT, LONG, IMPORTANT, PREVIOUS_EXAM

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    private Integer marks;
    private String difficulty;

    @Column(name = "is_ai", nullable = false)
    private boolean ai;

    @Column(name = "source_type")
    private String sourceType;
    @Column(name = "source_name")
    private String sourceName;
    @Column(name = "source_url")
    private String sourceUrl;
    @Column(name = "academic_year")
    private String academicYear;
    @Column(nullable = false)
    private int version = 1;
    private String license;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING_REVIEW;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public UUID getId() { return id; }
    public UUID getChapterId() { return chapterId; }
    public void setChapterId(UUID chapterId) { this.chapterId = chapterId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public boolean isAi() { return ai; }
    public void setAi(boolean ai) { this.ai = ai; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String v) { this.sourceType = v; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String v) { this.sourceName = v; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String v) { this.sourceUrl = v; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String v) { this.academicYear = v; }
    public int getVersion() { return version; }
    public void setVersion(int v) { this.version = v; }
    public String getLicense() { return license; }
    public void setLicense(String v) { this.license = v; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus v) { this.verificationStatus = v; }
}
