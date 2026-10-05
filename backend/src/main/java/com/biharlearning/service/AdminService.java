package com.biharlearning.service;

import com.biharlearning.entity.ContentChangeLog;
import com.biharlearning.entity.ContentSource;
import com.biharlearning.entity.SyncJob;
import com.biharlearning.exception.NotFoundException;
import com.biharlearning.repository.*;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Admin read/write operations: sources, review queue, stats. */
@Service
public class AdminService {

    private final ContentSourceRepository sourceRepo;
    private final SyncJobRepository jobRepo;
    private final ContentChangeLogRepository changeRepo;
    private final BookRepository bookRepo;
    private final SubjectRepository subjectRepo;
    private final ChapterRepository chapterRepo;
    private final ChapterContentRepository contentRepo;

    public AdminService(ContentSourceRepository sourceRepo, SyncJobRepository jobRepo,
                        ContentChangeLogRepository changeRepo, BookRepository bookRepo,
                        SubjectRepository subjectRepo, ChapterRepository chapterRepo,
                        ChapterContentRepository contentRepo) {
        this.sourceRepo = sourceRepo;
        this.jobRepo = jobRepo;
        this.changeRepo = changeRepo;
        this.bookRepo = bookRepo;
        this.subjectRepo = subjectRepo;
        this.chapterRepo = chapterRepo;
        this.contentRepo = contentRepo;
    }

    public List<ContentSource> listSources() { return sourceRepo.findAll(); }

    public List<SyncJob> listJobs() { return jobRepo.findTop50ByOrderByCreatedAtDesc(); }

    public List<ContentChangeLog> reviewQueue() {
        return changeRepo.findByStatusOrderByDetectedAtDesc("PENDING_REVIEW");
    }

    public ContentChangeLog approveChange(UUID changeId) {
        ContentChangeLog c = changeRepo.findById(changeId)
                .orElseThrow(() -> new NotFoundException("CONTENT_NOT_FOUND", "Change not found"));
        c.setStatus("APPROVED");
        c.setProcessedAt(OffsetDateTime.now());
        return changeRepo.save(c);
    }

    public ContentChangeLog rejectChange(UUID changeId) {
        ContentChangeLog c = changeRepo.findById(changeId)
                .orElseThrow(() -> new NotFoundException("CONTENT_NOT_FOUND", "Change not found"));
        c.setStatus("REJECTED");
        c.setProcessedAt(OffsetDateTime.now());
        return changeRepo.save(c);
    }

    public Map<String, Object> stats() {
        return Map.of(
                "books", bookRepo.count(),
                "subjects", subjectRepo.count(),
                "chapters", chapterRepo.count(),
                "sources", sourceRepo.count(),
                "syncSuccess", jobRepo.countByStatus("SUCCESS"),
                "syncFailed", jobRepo.countByStatus("FAILED"),
                "pendingReview", changeRepo.countByStatus("PENDING_REVIEW")
        );
    }

    public List<com.biharlearning.entity.ChapterContent> aiReviewQueue() {
        return contentRepo.findByAiReviewStatus("PENDING_REVIEW");
    }
}
