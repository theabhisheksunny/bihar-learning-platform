package com.biharlearning.service;

import com.biharlearning.dto.Dtos.SearchResultDto;
import com.biharlearning.repository.SearchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    private final SearchRepository searchRepo;

    public SearchService(SearchRepository searchRepo) {
        this.searchRepo = searchRepo;
    }

    public List<SearchResultDto> search(String q, String type, int page, int size) {
        int limit = Math.min(Math.max(size, 1), 50);
        int offset = Math.max(page, 0) * limit;
        return searchRepo.search(q, type, limit, offset).stream()
                .map(h -> new SearchResultDto(h.type(), h.id(), h.title(), h.snippet(), h.rank()))
                .toList();
    }
}
