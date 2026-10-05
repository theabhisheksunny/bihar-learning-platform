package com.biharlearning.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "chapters")
public class Chapter {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "book_id", nullable = false)
    private UUID bookId;

    @Column(name = "chapter_number", nullable = false)
    private int chapterNumber;

    @Column(nullable = false)
    private String slug;

    @Column(nullable = false)
    private String title;

    @Column(name = "title_hi")
    private String titleHi;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "source_page_start")
    private Integer sourcePageStart;

    @Column(name = "source_page_end")
    private Integer sourcePageEnd;

    @Column(name = "pdf_reference")
    private String pdfReference;

    @Embedded
    private Provenance provenance = new Provenance();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public UUID getId() { return id; }
    public UUID getBookId() { return bookId; }
    public void setBookId(UUID bookId) { this.bookId = bookId; }
    public int getChapterNumber() { return chapterNumber; }
    public void setChapterNumber(int chapterNumber) { this.chapterNumber = chapterNumber; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTitleHi() { return titleHi; }
    public void setTitleHi(String titleHi) { this.titleHi = titleHi; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getSourcePageStart() { return sourcePageStart; }
    public void setSourcePageStart(Integer v) { this.sourcePageStart = v; }
    public Integer getSourcePageEnd() { return sourcePageEnd; }
    public void setSourcePageEnd(Integer v) { this.sourcePageEnd = v; }
    public String getPdfReference() { return pdfReference; }
    public void setPdfReference(String pdfReference) { this.pdfReference = pdfReference; }
    public Provenance getProvenance() { return provenance; }
    public void setProvenance(Provenance provenance) { this.provenance = provenance; }
}
