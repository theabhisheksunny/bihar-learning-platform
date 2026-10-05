package com.biharlearning.dto;

import java.util.List;

/** Response DTOs (student-facing). Records keep them immutable and null-friendly. */
public final class Dtos {
    private Dtos() {}

    public record ProvenanceDto(
            String sourceType, String sourceName, String sourceUrl, String sourceDocumentUrl,
            String retrievedAt, String publishedAt, String academicYear, Integer version,
            String license, String verificationStatus) {}

    public record ClassDto(String id, String code, String name, boolean active, int displayOrder) {}

    public record SubjectDto(String id, String code, String name, String nameHi, String icon,
                             String language, String medium, String academicYear,
                             long bookCount, long chapterCount) {}

    public record BookSummaryDto(String id, String slug, String title, String subtitle,
                                 String subject, String language, String academicYear,
                                 boolean pdfAvailable, String coverImage,
                                 int chapterCount, String verificationStatus) {}

    public record BookDetailDto(String id, String slug, String title, String subtitle,
                                String board, String publisher, String language, String medium,
                                String edition, String isbn, String coverImage,
                                String academicYear, boolean pdfAvailable, boolean downloadAllowed,
                                String officialSourceUrl, String description, int chapterCount,
                                ProvenanceDto provenance, List<ChapterSummaryDto> chapters) {}

    public record ChapterSummaryDto(String id, int chapterNumber, String slug, String title,
                                    String titleHi, String verificationStatus) {}

    public record ChapterDetailDto(String id, int chapterNumber, String slug, String title,
                                   String titleHi, String description, String bookId,
                                   boolean pdfAvailable, ProvenanceDto provenance,
                                   List<String> availableSections) {}

    public record ContentDto(String id, String contentType, String body, String language,
                             boolean ai, String aiModel, String aiReviewStatus,
                             ProvenanceDto provenance) {}

    public record QuestionDto(String id, String category, String text, Integer marks,
                              String difficulty, boolean ai, String verificationStatus,
                              List<String> answers) {}

    public record McqDto(String id, String question, List<String> options, int correctIndex,
                         String explanation, boolean ai, String verificationStatus) {}

    public record SyllabusDto(String id, String title, String body, String documentUrl,
                              String sourceName, String sourceUrl, String academicYear,
                              String verificationStatus) {}

    public record SearchResultDto(String type, String id, String title, String snippet, double rank) {}

    public record PdfResolutionDto(String mode, String url, String sourceName, String license,
                                   String verificationStatus, String note,
                                   String accessMode, String storageProvider,
                                   Integer version, Long fileSize) {}

    public record StorageStatsDto(long totalFiles, long totalBytes, long pdfCount, long pdfBytes,
                                  long imageCount, long imageBytes, long currentVersionBytes,
                                  long archivedBytes, long externalDocuments,
                                  String quota, String usagePercent, String level,
                                  long candidatesForDeletion, long failedDocuments,
                                  String activeProvider) {}

    public record DocumentDto(String id, String documentHash, String fileName, String mimeType,
                              Long fileSize, String storageProvider, boolean hosted,
                              String hostingPermission, String accessMode, int version,
                              String status, boolean candidateForDeletion,
                              String sourceUrl, String officialSourceUrl) {}
}
