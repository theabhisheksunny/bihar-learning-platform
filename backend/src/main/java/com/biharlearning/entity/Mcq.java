package com.biharlearning.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "mcqs")
public class Mcq {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "chapter_id", nullable = false)
    private UUID chapterId;

    @Column(nullable = false, columnDefinition = "text")
    private String question;

    // JdbcTypeCode.JSON maps to jsonb on PostgreSQL (per the Flyway schema) and to a JSON/text
    // type on other engines, so no hard-coded columnDefinition is needed.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private List<String> options;

    @Column(name = "correct_index", nullable = false)
    private int correctIndex;

    @Column(columnDefinition = "text")
    private String explanation;

    @Column(name = "is_ai", nullable = false)
    private boolean ai;

    @Column(name = "source_type")
    private String sourceType;
    @Column(name = "source_name")
    private String sourceName;

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
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public int getCorrectIndex() { return correctIndex; }
    public void setCorrectIndex(int correctIndex) { this.correctIndex = correctIndex; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public boolean isAi() { return ai; }
    public void setAi(boolean ai) { this.ai = ai; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String v) { this.sourceType = v; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String v) { this.sourceName = v; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus v) { this.verificationStatus = v; }
}
