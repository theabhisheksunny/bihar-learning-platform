package com.biharlearning.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "sync_jobs")
public class SyncJob {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Column(nullable = false)
    private String status = "QUEUED"; // QUEUED, RUNNING, SUCCESS, FAILED

    @Column(name = "started_at")
    private OffsetDateTime startedAt;
    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Column(name = "records_discovered", nullable = false)
    private int recordsDiscovered;
    @Column(name = "records_updated", nullable = false)
    private int recordsUpdated;
    @Column(name = "records_failed", nullable = false)
    private int recordsFailed;

    @Column(name = "triggered_by")
    private String triggeredBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public UUID getId() { return id; }
    public UUID getSourceId() { return sourceId; }
    public void setSourceId(UUID sourceId) { this.sourceId = sourceId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime v) { this.startedAt = v; }
    public OffsetDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(OffsetDateTime v) { this.finishedAt = v; }
    public int getRecordsDiscovered() { return recordsDiscovered; }
    public void setRecordsDiscovered(int v) { this.recordsDiscovered = v; }
    public int getRecordsUpdated() { return recordsUpdated; }
    public void setRecordsUpdated(int v) { this.recordsUpdated = v; }
    public int getRecordsFailed() { return recordsFailed; }
    public void setRecordsFailed(int v) { this.recordsFailed = v; }
    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String v) { this.triggeredBy = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
