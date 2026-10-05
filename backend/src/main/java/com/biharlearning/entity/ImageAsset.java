package com.biharlearning.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Image metadata, deduplicated by checksum + variant (docs 17, 18). Binary not stored in PG. */
@Entity
@Table(name = "images")
public class ImageAsset {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "image_hash", nullable = false)
    private String imageHash;

    @Column(name = "mime_type")
    private String mimeType;
    @Column(name = "file_size")
    private Long fileSize;
    private Integer width;
    private Integer height;
    private String variant; // thumbnail, card, desktop, mobile

    @Column(name = "storage_provider", nullable = false)
    private String storageProvider = "EXTERNAL";
    @Column(name = "storage_path")
    private String storagePath;
    @Column(name = "source_url")
    private String sourceUrl;
    @Column(name = "is_hosted", nullable = false)
    private boolean hosted;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public UUID getId() { return id; }
    public String getImageHash() { return imageHash; }
    public void setImageHash(String v) { this.imageHash = v; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String v) { this.mimeType = v; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long v) { this.fileSize = v; }
    public Integer getWidth() { return width; }
    public void setWidth(Integer v) { this.width = v; }
    public Integer getHeight() { return height; }
    public void setHeight(Integer v) { this.height = v; }
    public String getVariant() { return variant; }
    public void setVariant(String v) { this.variant = v; }
    public String getStorageProvider() { return storageProvider; }
    public void setStorageProvider(String v) { this.storageProvider = v; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String v) { this.storagePath = v; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String v) { this.sourceUrl = v; }
    public boolean isHosted() { return hosted; }
    public void setHosted(boolean v) { this.hosted = v; }
}
