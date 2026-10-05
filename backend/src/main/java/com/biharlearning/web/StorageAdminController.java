package com.biharlearning.web;

import com.biharlearning.dto.Dtos.DocumentDto;
import com.biharlearning.dto.Dtos.StorageStatsDto;
import com.biharlearning.service.StorageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Admin storage dashboard endpoints (docs sections 19, 38). Requires ROLE_ADMIN. */
@RestController
@RequestMapping("/api/v1/admin/storage")
@PreAuthorize("hasRole('ADMIN')")
public class StorageAdminController {

    private final StorageService storage;

    public StorageAdminController(StorageService storage) {
        this.storage = storage;
    }

    @GetMapping("/stats")
    public ApiResponse<StorageStatsDto> stats() {
        return ApiResponse.ok(storage.stats());
    }

    @GetMapping("/documents")
    public ApiResponse<List<DocumentDto>> documents() {
        return ApiResponse.ok(storage.listDocuments());
    }

    @GetMapping("/duplicates")
    public ApiResponse<List<DocumentDto>> duplicates() {
        return ApiResponse.ok(storage.duplicatesCandidates());
    }

    @GetMapping("/orphans")
    public ApiResponse<List<DocumentDto>> orphans() {
        return ApiResponse.ok(storage.orphans());
    }

    @GetMapping("/failed")
    public ApiResponse<List<DocumentDto>> failed() {
        return ApiResponse.ok(storage.failed());
    }
}
