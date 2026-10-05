package com.biharlearning.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Reusable many-to-many link so many books can reference the SAME deduplicated document. */
@Entity
@Table(name = "book_documents")
public class BookDocument {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "book_id", nullable = false)
    private UUID bookId;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(nullable = false)
    private String role = "TEXTBOOK";

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public UUID getId() { return id; }
    public UUID getBookId() { return bookId; }
    public void setBookId(UUID v) { this.bookId = v; }
    public UUID getDocumentId() { return documentId; }
    public void setDocumentId(UUID v) { this.documentId = v; }
    public String getRole() { return role; }
    public void setRole(String v) { this.role = v; }
}
