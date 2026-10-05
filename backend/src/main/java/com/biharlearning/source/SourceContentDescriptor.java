package com.biharlearning.source;

/**
 * Normalized item returned by any SourceAdapter. Adapters translate their source's native
 * shape into this common model so downstream sync code is source-agnostic.
 * Unverified fields are null - never fabricated.
 */
public record SourceContentDescriptor(
        String kind,          // BOOK, CHAPTER, SYLLABUS
        String externalId,    // stable id within the source (e.g. url or code)
        String title,
        String subjectCode,   // our subject code if known, else null
        Integer chapterNumber,
        String pageUrl,       // human page
        String documentUrl,   // direct PDF, if known
        String academicYear,
        String license,
        String checksum
) {}
