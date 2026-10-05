package com.biharlearning.web;

import com.biharlearning.entity.ChapterContent;
import com.biharlearning.entity.ContentChangeLog;
import com.biharlearning.entity.ContentSource;
import com.biharlearning.entity.SyncJob;
import com.biharlearning.service.AdminService;
import com.biharlearning.service.AuthService;
import com.biharlearning.service.SyncService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService admin;
    private final SyncService sync;
    private final AuthService auth;

    public AdminController(AdminService admin, SyncService sync, AuthService auth) {
        this.admin = admin;
        this.sync = sync;
        this.auth = auth;
    }

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
    public record TokenResponse(String token, String role) {}

    @PostMapping("/auth/login")
    public ApiResponse<TokenResponse> login(@org.springframework.web.bind.annotation.RequestBody
                                            @jakarta.validation.Valid LoginRequest req) {
        String token = auth.login(req.email(), req.password());
        return ApiResponse.ok(new TokenResponse(token, "ADMIN"));
    }

    @GetMapping("/sources")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<ContentSource>> sources() {
        return ApiResponse.ok(admin.listSources());
    }

    @PostMapping("/sources/{id}/sync")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SyncJob> triggerSync(@PathVariable UUID id, Authentication authn) {
        String who = authn != null ? authn.getName() : "admin";
        return ApiResponse.ok(sync.runSync(id, who));
    }

    @GetMapping("/sync-jobs")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<SyncJob>> jobs() {
        return ApiResponse.ok(admin.listJobs());
    }

    @GetMapping("/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<ContentChangeLog>> reviewQueue() {
        return ApiResponse.ok(admin.reviewQueue());
    }

    @PostMapping("/review/{changeId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ContentChangeLog> approve(@PathVariable UUID changeId) {
        return ApiResponse.ok(admin.approveChange(changeId));
    }

    @PostMapping("/review/{changeId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ContentChangeLog> reject(@PathVariable UUID changeId) {
        return ApiResponse.ok(admin.rejectChange(changeId));
    }

    @GetMapping("/ai-content")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<ChapterContent>> aiContent() {
        return ApiResponse.ok(admin.aiReviewQueue());
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> stats() {
        return ApiResponse.ok(admin.stats());
    }
}
