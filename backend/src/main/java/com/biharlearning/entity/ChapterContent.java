package com.biharlearning.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "chapter_contents")
public class ChapterContent {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "chapter_id", nullable = false)
    private UUID chapterId;

    @Column(name = "content_type", nullable = false)
    private String contentType; // SUMMARY, KEY_CONCEPTS, DEFINITIONS, IMPORTANT_POINTS, REVISION, FORMULAS

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @Column(nullable = false)
    private String language = "en";

    @Column(name = "is_ai", nullable = false)
    private boolean ai;

    @Column(name = "ai_generated_at")
    private OffsetDateTime aiGeneratedAt;

    @Column(name = "ai_model")
    private String aiModel;

    @Column(name = "ai_source_content_id")
    private UUID aiSourceContentId;

    @Column(name = "ai_prompt_version")
    private String aiPromptVersion;

    @Column(name = "ai_review_status")
    private String aiReviewStatus;

    @Embedded
    private Provenance provenance = new Provenance();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public UUID getId() { return id; }
    public UUID getChapterId() { return chapterId; }
    public void setChapterId(UUID chapterId) { this.chapterId = chapterId; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public boolean isAi() { return ai; }
    public void setAi(boolean ai) { this.ai = ai; }
    public OffsetDateTime getAiGeneratedAt() { return aiGeneratedAt; }
    public void setAiGeneratedAt(OffsetDateTime v) { this.aiGeneratedAt = v; }
    public String getAiModel() { return aiModel; }
    public void setAiModel(String v) { this.aiModel = v; }
    public UUID getAiSourceContentId() { return aiSourceContentId; }
    public void setAiSourceContentId(UUID v) { this.aiSourceContentId = v; }
    public String getAiPromptVersion() { return aiPromptVersion; }
    public void setAiPromptVersion(String v) { this.aiPromptVersion = v; }
    public String getAiReviewStatus() { return aiReviewStatus; }
    public void setAiReviewStatus(String v) { this.aiReviewStatus = v; }
    public Provenance getProvenance() { return provenance; }
    public void setProvenance(Provenance provenance) { this.provenance = provenance; }
}
