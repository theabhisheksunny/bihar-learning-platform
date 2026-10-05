package com.biharlearning.web;

import com.biharlearning.dto.Dtos.SearchResultDto;
import com.biharlearning.service.SearchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public ApiResponse<List<SearchResultDto>> search(@RequestParam String q,
                                                     @RequestParam(required = false) String type,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(searchService.search(q, type, page, size));
    }
}
