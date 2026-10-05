package com.biharlearning.service;

import com.biharlearning.entity.*;
import com.biharlearning.exception.BusinessException;
import com.biharlearning.exception.NotFoundException;
import com.biharlearning.repository.*;
import com.biharlearning.source.SourceAdapter;
import com.biharlearning.source.SourceContentDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Content synchronization engine. Discovers from an enabled, robots-confirmed source via its
 * adapter, then records every discovered item as a PENDING_REVIEW entry in content_change_log.
 * It NEVER auto-overwrites OFFICIAL content: an admin approves changes separately. Live crawling
 * is disabled unless app.sync.enabled=true AND the source is enabled + robots-confirmed.
 */
@Service
public class SyncService {

    private static final Logger log = LoggerFactory.getLogger(SyncService.class);

    private final boolean syncEnabled;
    private final ContentSourceRepository sourceRepo;
    private final SyncJobRepository jobRepo;
    private final ContentChangeLogRepository changeRepo;
    private final ApplicationContext ctx;

    public SyncService(@Value("${app.sync.enabled:false}") boolean syncEnabled,
                       ContentSourceRepository sourceRepo, SyncJobRepository jobRepo,
                       ContentChangeLogRepository changeRepo, ApplicationContext ctx) {
        this.syncEnabled = syncEnabled;
        this.sourceRepo = sourceRepo;
        this.jobRepo = jobRepo;
        this.changeRepo = changeRepo;
        this.ctx = ctx;
    }

    /** Trigger a sync for a source by id. Returns the created job. */
    public SyncJob runSync(UUID sourceId, String triggeredBy) {
        ContentSource source = sourceRepo.findById(sourceId)
                .orElseThrow(() -> new NotFoundException("SOURCE_NOT_FOUND", "Content source not found"));

        if (!syncEnabled) {
            throw new BusinessException("SOURCE_DISABLED",
                    "Live sync is disabled globally (app.sync.enabled=false). Enable it only after "
                            + "confirming each source's robots.txt and terms.", HttpStatus.CONFLICT);
        }
        if (!source.isEnabled()) {
            throw new BusinessException("SOURCE_DISABLED",
                    "Source '" + source.getName() + "' is disabled.", HttpStatus.CONFLICT);
        }
        if (!source.isRobotsConfirmed()) {
            throw new BusinessException("ROBOTS_NOT_CONFIRMED",
                    "robots.txt / terms not confirmed for '" + source.getName() + "'.", HttpStatus.CONFLICT);
        }

        SyncJob job = new SyncJob();
        job.setSourceId(sourceId);
        job.setStatus("RUNNING");
        job.setStartedAt(OffsetDateTime.now());
        job.setTriggeredBy(triggeredBy);
        job = jobRepo.save(job);

        try {
            SourceAdapter adapter = resolveAdapter(source);
            List<SourceContentDescriptor> items = adapter.discover("9");
            int discovered = items.size();
            int recorded = 0;
            for (SourceContentDescriptor d : items) {
                ContentChangeLog change = new ContentChangeLog();
                change.setContentTable(d.kind().equalsIgnoreCase("CHAPTER") ? "chapters" : "books");
                change.setSourceId(sourceId);
                change.setChangeType("NEW");
                change.setNewVersion(1);
                change.setStatus("PENDING_REVIEW");
                changeRepo.save(change);
                recorded++;
            }
            job.setRecordsDiscovered(discovered);
            job.setRecordsUpdated(recorded);
            job.setStatus("SUCCESS");
            source.setLastSuccessAt(OffsetDateTime.now());
        } catch (Exception e) {
            log.error("Sync failed for source {}: {}", source.getName(), e.getMessage());
            job.setStatus("FAILED");
            job.setRecordsFailed(1);
        } finally {
            job.setFinishedAt(OffsetDateTime.now());
            source.setLastCheckedAt(OffsetDateTime.now());
            sourceRepo.save(source);
            jobRepo.save(job);
        }
        return job;
    }

    private SourceAdapter resolveAdapter(ContentSource source) {
        if (source.getAdapterBean() == null) {
            throw new BusinessException("SOURCE_DISABLED",
                    "No adapter registered for source '" + source.getName() + "'.", HttpStatus.CONFLICT);
        }
        try {
            return ctx.getBean(source.getAdapterBean(), SourceAdapter.class);
        } catch (Exception e) {
            throw new BusinessException("SOURCE_DISABLED",
                    "Adapter bean '" + source.getAdapterBean() + "' not available.", HttpStatus.CONFLICT);
        }
    }
}
