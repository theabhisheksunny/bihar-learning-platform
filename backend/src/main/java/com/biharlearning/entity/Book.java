package com.biharlearning.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(name = "source_id")
    private UUID sourceId;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String title;

    private String subtitle;
    private String board;
    private String publisher;

    @Column(nullable = false)
    private String language = "en";

    private String medium;
    private String edition;
    private String isbn;

    @Column(name = "cover_image")
    private String coverImage;

    @Column(name = "pdf_url")
    private String pdfUrl;

    @Column(name = "official_source_url")
    private String officialSourceUrl;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "chapter_count", nullable = false)
    private int chapterCount;

    @Column(name = "download_allowed", nullable = false)
    private boolean downloadAllowed;

    @Column(nullable = false)
    private String status = "DRAFT";

    @Column(name = "last_updated")
    private OffsetDateTime lastUpdated;

    @Embedded
    private Provenance provenance = new Provenance();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public UUID getId() { return id; }
    public UUID getSubjectId() { return subjectId; }
    public void setSubjectId(UUID subjectId) { this.subjectId = subjectId; }
    public UUID getSourceId() { return sourceId; }
    public void setSourceId(UUID sourceId) { this.sourceId = sourceId; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getBoard() { return board; }
    public void setBoard(String board) { this.board = board; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getMedium() { return medium; }
    public void setMedium(String medium) { this.medium = medium; }
    public String getEdition() { return edition; }
    public void setEdition(String edition) { this.edition = edition; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }
    public String getOfficialSourceUrl() { return officialSourceUrl; }
    public void setOfficialSourceUrl(String officialSourceUrl) { this.officialSourceUrl = officialSourceUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getChapterCount() { return chapterCount; }
    public void setChapterCount(int chapterCount) { this.chapterCount = chapterCount; }
    public boolean isDownloadAllowed() { return downloadAllowed; }
    public void setDownloadAllowed(boolean downloadAllowed) { this.downloadAllowed = downloadAllowed; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(OffsetDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    public Provenance getProvenance() { return provenance; }
    public void setProvenance(Provenance provenance) { this.provenance = provenance; }
}
