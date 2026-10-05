package com.biharlearning.web;

import com.biharlearning.dto.Dtos.*;
import com.biharlearning.service.ContentService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class CatalogController {

    private final ContentService content;

    public CatalogController(ContentService content) {
        this.content = content;
    }

    @GetMapping("/classes")
    public ApiResponse<List<ClassDto>> classes() {
        return ApiResponse.ok(content.listClasses());
    }

    @GetMapping("/classes/{classId}")
    public ApiResponse<ClassDto> classDetail(@PathVariable String classId) {
        return ApiResponse.ok(content.getClass(classId));
    }

    @GetMapping("/classes/{classId}/subjects")
    public ApiResponse<List<SubjectDto>> subjects(@PathVariable String classId) {
        return ApiResponse.ok(content.listSubjects(classId));
    }

    @GetMapping("/subjects/{subjectId}")
    public ApiResponse<SubjectDto> subject(@PathVariable String subjectId) {
        return ApiResponse.ok(content.getSubject(subjectId));
    }

    @GetMapping("/subjects/{subjectId}/books")
    public ApiResponse<List<BookSummaryDto>> books(@PathVariable String subjectId,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        Page<BookSummaryDto> p = content.listBooks(subjectId, page, size);
        return ApiResponse.ok(p.getContent(), Map.of(
                "page", p.getNumber(), "size", p.getSize(),
                "totalElements", p.getTotalElements(), "totalPages", p.getTotalPages()));
    }

    @GetMapping("/books/{bookId}")
    public ApiResponse<BookDetailDto> book(@PathVariable String bookId) {
        return ApiResponse.ok(content.getBook(bookId));
    }

    @GetMapping("/books/{bookId}/chapters")
    public ApiResponse<List<ChapterSummaryDto>> chapters(@PathVariable String bookId) {
        return ApiResponse.ok(content.listChapters(bookId));
    }

    @GetMapping("/chapters/{chapterId}")
    public ApiResponse<ChapterDetailDto> chapter(@PathVariable String chapterId) {
        return ApiResponse.ok(content.getChapter(chapterId));
    }

    @GetMapping("/chapters/{chapterId}/summary")
    public ApiResponse<List<ContentDto>> summary(@PathVariable String chapterId) {
        return ApiResponse.ok(content.getChapterContent(chapterId, "SUMMARY"));
    }

    @GetMapping("/chapters/{chapterId}/content")
    public ApiResponse<List<ContentDto>> chapterContent(@PathVariable String chapterId,
                                                        @RequestParam(required = false) String type) {
        return ApiResponse.ok(content.getChapterContent(chapterId, type));
    }

    @GetMapping("/chapters/{chapterId}/questions")
    public ApiResponse<List<QuestionDto>> questions(@PathVariable String chapterId,
                                                    @RequestParam(required = false) String category) {
        return ApiResponse.ok(content.getQuestions(chapterId, category));
    }

    @GetMapping("/chapters/{chapterId}/answers")
    public ApiResponse<List<QuestionDto>> answers(@PathVariable String chapterId) {
        // Answers are embedded with their questions.
        return ApiResponse.ok(content.getQuestions(chapterId, null));
    }

    @GetMapping("/chapters/{chapterId}/mcqs")
    public ApiResponse<List<McqDto>> mcqs(@PathVariable String chapterId) {
        return ApiResponse.ok(content.getMcqs(chapterId));
    }

    @GetMapping("/syllabus/{classId}")
    public ApiResponse<List<SyllabusDto>> syllabus(@PathVariable String classId,
                                                   @RequestParam(required = false) String subjectId) {
        return ApiResponse.ok(content.getSyllabus(classId, subjectId));
    }
}
