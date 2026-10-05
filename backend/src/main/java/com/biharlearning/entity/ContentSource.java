package com.biharlearning.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "content_sources")
public class ContentSource {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "source_key", nullable = false, unique = true)
    private String sourceKey;

    @Column(nullable = false)
    private String name;

    @Column(name = "authority_tier", nullable = false)
    private int authorityTier = 1;

    @Column(name = "base_url")
    private String baseUrl;

    @Column(name = "adapter_bean")
    private String adapterBean;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "robots_confirmed", nullable = false)
    private boolean robotsConfirmed;

    @Column(name = "license_default")
    private String licenseDefault;

    @Column(name = "last_checked_at")
    private OffsetDateTime lastCheckedAt;

    @Column(name = "last_success_at")
    private OffsetDateTime lastSuccessAt;

    // Provenance fields (docs section 4).
    @Column(name = "source_type")
    private String sourceType;        // GOVERNMENT, EDUCATIONAL, THIRD_PARTY
    @Column(name = "authority_level")
    private String authorityLevel;    // OFFICIAL, VERIFIED, UNVERIFIED
    private String license;
    @Column(name = "terms_url")
    private String termsUrl;
    @Column(name = "robots_status")
    private String robotsStatus;      // UNKNOWN, ALLOWED, DISALLOWED
    @Column(name = "api_available", nullable = false)
    private boolean apiAvailable;
    @Column(name = "api_documentation_url")
    private String apiDocumentationUrl;
    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public UUID getId() { return id; }
    public String getSourceKey() { return sourceKey; }
    public void setSourceKey(String v) { this.sourceKey = v; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAuthorityTier() { return authorityTier; }
    public void setAuthorityTier(int v) { this.authorityTier = v; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String v) { this.baseUrl = v; }
    public String getAdapterBean() { return adapterBean; }
    public void setAdapterBean(String v) { this.adapterBean = v; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isRobotsConfirmed() { return robotsConfirmed; }
    public void setRobotsConfirmed(boolean v) { this.robotsConfirmed = v; }
    public String getLicenseDefault() { return licenseDefault; }
    public void setLicenseDefault(String v) { this.licenseDefault = v; }
    public OffsetDateTime getLastCheckedAt() { return lastCheckedAt; }
    public void setLastCheckedAt(OffsetDateTime v) { this.lastCheckedAt = v; }
    public OffsetDateTime getLastSuccessAt() { return lastSuccessAt; }
    public void setLastSuccessAt(OffsetDateTime v) { this.lastSuccessAt = v; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String v) { this.sourceType = v; }
    public String getAuthorityLevel() { return authorityLevel; }
    public void setAuthorityLevel(String v) { this.authorityLevel = v; }
    public String getLicense() { return license; }
    public void setLicense(String v) { this.license = v; }
    public String getTermsUrl() { return termsUrl; }
    public void setTermsUrl(String v) { this.termsUrl = v; }
    public String getRobotsStatus() { return robotsStatus; }
    public void setRobotsStatus(String v) { this.robotsStatus = v; }
    public boolean isApiAvailable() { return apiAvailable; }
    public void setApiAvailable(boolean v) { this.apiAvailable = v; }
    public String getApiDocumentationUrl() { return apiDocumentationUrl; }
    public void setApiDocumentationUrl(String v) { this.apiDocumentationUrl = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
}
