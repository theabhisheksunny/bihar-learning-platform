package com.biharlearning.service;

import com.biharlearning.dto.Dtos.BookDetailDto;
import com.biharlearning.dto.Dtos.ChapterSummaryDto;
import com.biharlearning.dto.Dtos.SubjectDto;
import com.biharlearning.entity.*;
import com.biharlearning.mapper.ContentMapper;
import com.biharlearning.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the read-model against a real (H2/PostgreSQL-mode) database. Exercises
 * JPA mappings for Class -> Subject -> Book -> Chapter and the ContentService read paths
 * (slug/code resolution, counts, DTO mapping). The Postgres-only full-text search and jsonb MCQ
 * paths are covered separately by unit/web tests; this focuses on portable ORM + service logic.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({ContentService.class, ContentMapper.class})
class ContentServiceIntegrationTest {

    @Autowired ClassRepository classRepo;
    @Autowired SubjectRepository subjectRepo;
    @Autowired BookRepository bookRepo;
    @Autowired ChapterRepository chapterRepo;
    @Autowired ContentService content;

    private String bookSlug;

    @BeforeEach
    void seed() {
        ClassEntity cls = new ClassEntity();
        cls.setCode("9");
        cls.setName("Class IX");
        cls.setActive(true);
        cls.setDisplayOrder(4);
        cls = classRepo.save(cls);

        Subject subj = new Subject();
        subj.setClassId(cls.getId());
        subj.setCode("mathematics");
        subj.setName("Mathematics");
        subj.setNameHi("गणित");
        subj.setDisplayOrder(1);
        subj = subjectRepo.save(subj);

        Book book = new Book();
        book.setSubjectId(subj.getId());
        book.setSlug("bihar-class-9-mathematics");
        book.setTitle("Mathematics");
        book.getProvenance().setVerificationStatus(VerificationStatus.PENDING_REVIEW);
        book.getProvenance().setSourceName("NCERT");
        book = bookRepo.save(book);
        bookSlug = book.getSlug();

        Chapter ch1 = new Chapter();
        ch1.setBookId(book.getId());
        ch1.setChapterNumber(1);
        ch1.setSlug("number-systems");
        ch1.setTitle("Number Systems");
        chapterRepo.save(ch1);

        Chapter ch2 = new Chapter();
        ch2.setBookId(book.getId());
        ch2.setChapterNumber(2);
        ch2.setSlug("polynomials");
        ch2.setTitle("Polynomials");
        chapterRepo.save(ch2);

        book.setChapterCount(2);
        bookRepo.save(book);
    }

    @Test
    void listsClassesAndResolvesByCode() {
        assertFalse(content.listClasses().isEmpty());
        assertEquals("Class IX", content.getClass("9").name());
    }

    @Test
    void listsSubjectsWithCounts() {
        List<SubjectDto> subjects = content.listSubjects("9");
        assertEquals(1, subjects.size());
        SubjectDto math = subjects.get(0);
        assertEquals("Mathematics", math.name());
        assertEquals(1, math.bookCount());
        assertEquals(2, math.chapterCount());
    }

    @Test
    void resolvesBookBySlugWithOrderedChapters() {
        BookDetailDto book = content.getBook(bookSlug);
        assertEquals("Mathematics", book.title());
        assertEquals(2, book.chapterCount());
        List<ChapterSummaryDto> chapters = book.chapters();
        assertEquals(2, chapters.size());
        assertEquals(1, chapters.get(0).chapterNumber());
        assertEquals("Number Systems", chapters.get(0).title());
        assertEquals("NCERT", book.provenance().sourceName());
    }
}
