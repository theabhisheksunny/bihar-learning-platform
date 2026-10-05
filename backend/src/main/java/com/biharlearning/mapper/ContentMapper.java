package com.biharlearning.mapper;

import com.biharlearning.dto.Dtos.*;
import com.biharlearning.entity.*;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

/** Maps entities to student-facing DTOs. Null-safe: unverified fields stay null. */
@Component
public class ContentMapper {

    public ClassDto toClassDto(ClassEntity c) {
        return new ClassDto(c.getId().toString(), c.getCode(), c.getName(), c.isActive(), c.getDisplayOrder());
    }

    public SubjectDto toSubjectDto(Subject s, long bookCount, long chapterCount) {
        return new SubjectDto(s.getId().toString(), s.getCode(), s.getName(), s.getNameHi(),
                s.getIcon(), s.getLanguage(), s.getMedium(), s.getAcademicYear(), bookCount, chapterCount);
    }

    public BookSummaryDto toBookSummary(Book b, String subjectName) {
        return new BookSummaryDto(b.getId().toString(), b.getSlug(), b.getTitle(), b.getSubtitle(),
                subjectName, b.getLanguage(), b.getProvenance().getAcademicYear(),
                hasPdf(b), b.getCoverImage(), b.getChapterCount(),
                b.getProvenance().getVerificationStatus().name());
    }

    public BookDetailDto toBookDetail(Book b, List<ChapterSummaryDto> chapters) {
        return new BookDetailDto(b.getId().toString(), b.getSlug(), b.getTitle(), b.getSubtitle(),
                b.getBoard(), b.getPublisher(), b.getLanguage(), b.getMedium(), b.getEdition(),
                b.getIsbn(), b.getCoverImage(), b.getProvenance().getAcademicYear(),
                hasPdf(b), b.isDownloadAllowed(), b.getOfficialSourceUrl(), b.getDescription(),
                b.getChapterCount(), toProvenance(b.getProvenance()), chapters);
    }

    public ChapterSummaryDto toChapterSummary(Chapter c) {
        return new ChapterSummaryDto(c.getId().toString(), c.getChapterNumber(), c.getSlug(),
                c.getTitle(), c.getTitleHi(), c.getProvenance().getVerificationStatus().name());
    }

    public ChapterDetailDto toChapterDetail(Chapter c, boolean pdfAvailable, List<String> sections) {
        return new ChapterDetailDto(c.getId().toString(), c.getChapterNumber(), c.getSlug(),
                c.getTitle(), c.getTitleHi(), c.getDescription(), c.getBookId().toString(),
                pdfAvailable, toProvenance(c.getProvenance()), sections);
    }

    public ContentDto toContentDto(ChapterContent cc) {
        return new ContentDto(cc.getId().toString(), cc.getContentType(), cc.getBody(),
                cc.getLanguage(), cc.isAi(), cc.getAiModel(), cc.getAiReviewStatus(),
                toProvenance(cc.getProvenance()));
    }

    public QuestionDto toQuestionDto(Question q, List<String> answers) {
        return new QuestionDto(q.getId().toString(), q.getCategory(), q.getText(), q.getMarks(),
                q.getDifficulty(), q.isAi(), q.getVerificationStatus().name(), answers);
    }

    public McqDto toMcqDto(Mcq m) {
        return new McqDto(m.getId().toString(), m.getQuestion(), m.getOptions(), m.getCorrectIndex(),
                m.getExplanation(), m.isAi(), m.getVerificationStatus().name());
    }

    public SyllabusDto toSyllabusDto(Syllabus s) {
        return new SyllabusDto(s.getId().toString(), s.getTitle(), s.getBody(), s.getDocumentUrl(),
                s.getSourceName(), s.getSourceUrl(), s.getAcademicYear(),
                s.getVerificationStatus().name());
    }

    public ProvenanceDto toProvenance(Provenance p) {
        if (p == null) return null;
        return new ProvenanceDto(p.getSourceType(), p.getSourceName(), p.getSourceUrl(),
                p.getSourceDocumentUrl(), iso(p.getRetrievedAt()), iso(p.getPublishedAt()),
                p.getAcademicYear(), p.getVersion(), p.getLicense(),
                p.getVerificationStatus() == null ? null : p.getVerificationStatus().name());
    }

    private boolean hasPdf(Book b) {
        return (b.getPdfUrl() != null && !b.getPdfUrl().isBlank())
                || (b.getOfficialSourceUrl() != null && !b.getOfficialSourceUrl().isBlank())
                || (b.getProvenance() != null && b.getProvenance().getSourceDocumentUrl() != null);
    }

    private String iso(OffsetDateTime t) { return t == null ? null : t.toString(); }
}
