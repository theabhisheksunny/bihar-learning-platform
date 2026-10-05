package com.biharlearning.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Reusable link so many chapters can reference the SAME deduplicated document. */
@Entity
@Table(name = "chapter_documents")
public class ChapterDocument {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "chapter_id", nullable = false)
    private UUID chapterId;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(nullable = false)
    private String role = "CHAPTER_PDF";

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public UUID getId() { return id; }
    public UUID getChapterId() { return chapterId; }
    public void setChapterId(UUID v) { this.chapterId = v; }
    public UUID getDocumentId() { return documentId; }
    public void setDocumentId(UUID v) { this.documentId = v; }
    public String getRole() { return role; }
    public void setRole(String v) { this.role = v; }
}
