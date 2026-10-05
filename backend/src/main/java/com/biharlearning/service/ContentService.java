package com.biharlearning.service;

import com.biharlearning.dto.Dtos.*;
import com.biharlearning.entity.*;
import com.biharlearning.exception.NotFoundException;
import com.biharlearning.mapper.ContentMapper;
import com.biharlearning.repository.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Read-model service for the student-facing catalogue. Cached in Redis. */
@Service
public class ContentService {

    private final ClassRepository classRepo;
    private final SubjectRepository subjectRepo;
    private final BookRepository bookRepo;
    private final ChapterRepository chapterRepo;
    private final ChapterContentRepository contentRepo;
    private final QuestionRepository questionRepo;
    private final AnswerRepository answerRepo;
    private final McqRepository mcqRepo;
    private final SyllabusRepository syllabusRepo;
    private final ContentMapper mapper;

    public ContentService(ClassRepository classRepo, SubjectRepository subjectRepo,
                          BookRepository bookRepo, ChapterRepository chapterRepo,
                          ChapterContentRepository contentRepo, QuestionRepository questionRepo,
                          AnswerRepository answerRepo, McqRepository mcqRepo,
                          SyllabusRepository syllabusRepo, ContentMapper mapper) {
        this.classRepo = classRepo;
        this.subjectRepo = subjectRepo;
        this.bookRepo = bookRepo;
        this.chapterRepo = chapterRepo;
        this.contentRepo = contentRepo;
        this.questionRepo = questionRepo;
        this.answerRepo = answerRepo;
        this.mcqRepo = mcqRepo;
        this.syllabusRepo = syllabusRepo;
        this.mapper = mapper;
    }

    @Cacheable("classes")
    public List<ClassDto> listClasses() {
        return classRepo.findAllByOrderByDisplayOrderAsc().stream().map(mapper::toClassDto).toList();
    }

    @Cacheable(value = "class", key = "#classId")
    public ClassDto getClass(String classId) {
        return mapper.toClassDto(resolveClass(classId));
    }

    @Cacheable(value = "subjects", key = "#classId")
    public List<SubjectDto> listSubjects(String classId) {
        ClassEntity cls = resolveClass(classId);
        List<Subject> subjects = subjectRepo.findByClassIdOrderByDisplayOrderAsc(cls.getId());
        List<SubjectDto> out = new ArrayList<>();
        for (Subject s : subjects) {
            long books = bookRepo.countBySubjectId(s.getId());
            long chapters = bookRepo.findBySubjectId(s.getId(), PageRequest.of(0, 1000)).stream()
                    .mapToLong(b -> chapterRepo.countByBookId(b.getId())).sum();
            out.add(mapper.toSubjectDto(s, books, chapters));
        }
        return out;
    }

    public SubjectDto getSubject(String subjectId) {
        Subject s = subjectRepo.findById(UUID.fromString(subjectId))
                .orElseThrow(() -> new NotFoundException("SUBJECT_NOT_FOUND", "Subject could not be found"));
        long books = bookRepo.countBySubjectId(s.getId());
        long chapters = bookRepo.findBySubjectId(s.getId(), PageRequest.of(0, 1000)).stream()
                .mapToLong(b -> chapterRepo.countByBookId(b.getId())).sum();
        return mapper.toSubjectDto(s, books, chapters);
    }

    public Page<BookSummaryDto> listBooks(String subjectId, int page, int size) {
        Subject s = subjectRepo.findById(UUID.fromString(subjectId))
                .orElseThrow(() -> new NotFoundException("SUBJECT_NOT_FOUND", "Subject could not be found"));
        return bookRepo.findBySubjectId(s.getId(), PageRequest.of(page, size))
                .map(b -> mapper.toBookSummary(b, s.getName()));
    }

    @Cacheable(value = "book", key = "#bookId")
    public BookDetailDto getBook(String bookId) {
        Book b = resolveBook(bookId);
        String subjectName = subjectRepo.findById(b.getSubjectId()).map(Subject::getName).orElse(null);
        List<ChapterSummaryDto> chapters = chapterRepo.findByBookIdOrderByChapterNumberAsc(b.getId())
                .stream().map(mapper::toChapterSummary).toList();
        BookDetailDto dto = mapper.toBookDetail(b, chapters);
        // keep subject on summary line via description untouched; subject surfaced in chapters' parent
        return dto;
    }

    public List<ChapterSummaryDto> listChapters(String bookId) {
        Book b = resolveBook(bookId);
        return chapterRepo.findByBookIdOrderByChapterNumberAsc(b.getId())
                .stream().map(mapper::toChapterSummary).toList();
    }

    @Cacheable(value = "chapter", key = "#chapterId")
    public ChapterDetailDto getChapter(String chapterId) {
        Chapter c = resolveChapter(chapterId);
        List<String> sections = new ArrayList<>();
        List<ChapterContent> contents = contentRepo.findByChapterId(c.getId());
        contents.stream().map(ChapterContent::getContentType).distinct().forEach(sections::add);
        if (!questionRepo.findByChapterId(c.getId()).isEmpty()) sections.add("QUESTIONS");
        if (!mcqRepo.findByChapterId(c.getId()).isEmpty()) sections.add("MCQS");
        boolean pdf = c.getPdfReference() != null || c.getProvenance().getSourceDocumentUrl() != null;
        return mapper.toChapterDetail(c, pdf, sections);
    }

    public List<ContentDto> getChapterContent(String chapterId, String type) {
        Chapter c = resolveChapter(chapterId);
        List<ChapterContent> list = (type == null || type.isBlank())
                ? contentRepo.findByChapterId(c.getId())
                : contentRepo.findByChapterIdAndContentType(c.getId(), type);
        return list.stream().map(mapper::toContentDto).toList();
    }

    public List<QuestionDto> getQuestions(String chapterId, String category) {
        Chapter c = resolveChapter(chapterId);
        List<Question> qs = (category == null || category.isBlank())
                ? questionRepo.findByChapterId(c.getId())
                : questionRepo.findByChapterIdAndCategory(c.getId(), category);
        return qs.stream().map(q -> {
            List<String> answers = answerRepo.findByQuestionId(q.getId()).stream()
                    .map(Answer::getText).toList();
            return mapper.toQuestionDto(q, answers);
        }).toList();
    }

    public List<McqDto> getMcqs(String chapterId) {
        Chapter c = resolveChapter(chapterId);
        return mcqRepo.findByChapterId(c.getId()).stream().map(mapper::toMcqDto).toList();
    }

    public List<SyllabusDto> getSyllabus(String classId, String subjectId) {
        ClassEntity cls = resolveClass(classId);
        List<Syllabus> list = (subjectId == null || subjectId.isBlank())
                ? syllabusRepo.findByClassId(cls.getId())
                : syllabusRepo.findByClassIdAndSubjectId(cls.getId(), UUID.fromString(subjectId));
        return list.stream().map(mapper::toSyllabusDto).toList();
    }

    // --- resolvers (UUID or slug/code) ---

    private ClassEntity resolveClass(String id) {
        Optional<ClassEntity> byCode = classRepo.findByCode(id);
        if (byCode.isPresent()) return byCode.get();
        try {
            return classRepo.findById(UUID.fromString(id))
                    .orElseThrow(() -> new NotFoundException("CLASS_NOT_FOUND", "Class could not be found"));
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("CLASS_NOT_FOUND", "Class could not be found");
        }
    }

    Book resolveBook(String id) {
        Optional<Book> bySlug = bookRepo.findBySlug(id);
        if (bySlug.isPresent()) return bySlug.get();
        try {
            return bookRepo.findById(UUID.fromString(id))
                    .orElseThrow(() -> new NotFoundException("BOOK_NOT_FOUND", "Book could not be found"));
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("BOOK_NOT_FOUND", "Book could not be found");
        }
    }

    Chapter resolveChapter(String id) {
        try {
            return chapterRepo.findById(UUID.fromString(id))
                    .orElseThrow(() -> new NotFoundException("CHAPTER_NOT_FOUND", "Chapter could not be found"));
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("CHAPTER_NOT_FOUND", "Chapter could not be found");
        }
    }
}
