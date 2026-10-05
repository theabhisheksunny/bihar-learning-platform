package com.biharlearning.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "content_change_log")
public class ContentChangeLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "content_id")
    private UUID contentId;

    @Column(name = "content_table", nullable = false)
    private String contentTable;

    @Column(name = "source_id")
    private UUID sourceId;

    @Column(name = "old_version")
    private Integer oldVersion;
    @Column(name = "new_version")
    private Integer newVersion;

    @Column(name = "change_type", nullable = false)
    private String changeType; // NEW, UPDATED, REMOVED, RENAMED

    @Column(name = "detected_at", nullable = false)
    private OffsetDateTime detectedAt = OffsetDateTime.now();

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Column(nullable = false)
    private String status = "PENDING_REVIEW"; // PENDING_REVIEW, APPROVED, REJECTED

    public UUID getId() { return id; }
    public UUID getContentId() { return contentId; }
    public void setContentId(UUID v) { this.contentId = v; }
    public String getContentTable() { return contentTable; }
    public void setContentTable(String v) { this.contentTable = v; }
    public UUID getSourceId() { return sourceId; }
    public void setSourceId(UUID v) { this.sourceId = v; }
    public Integer getOldVersion() { return oldVersion; }
    public void setOldVersion(Integer v) { this.oldVersion = v; }
    public Integer getNewVersion() { return newVersion; }
    public void setNewVersion(Integer v) { this.newVersion = v; }
    public String getChangeType() { return changeType; }
    public void setChangeType(String v) { this.changeType = v; }
    public OffsetDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(OffsetDateTime v) { this.detectedAt = v; }
    public OffsetDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(OffsetDateTime v) { this.processedAt = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
}
