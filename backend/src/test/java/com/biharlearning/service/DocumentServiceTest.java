package com.biharlearning.service;

import com.biharlearning.entity.Book;
import com.biharlearning.entity.Chapter;
import com.biharlearning.entity.Document;
import com.biharlearning.entity.DocumentVersion;
import com.biharlearning.entity.Subject;
import com.biharlearning.entity.ClassEntity;
import com.biharlearning.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Section 39 (deduplication, versioning, orphan detection) against a real database.
 *
 *  - Duplicate detection: registering the same checksum twice stores ONE document; two books can
 *    both reference it (store once, reference many).
 *  - Changed document: a different checksum creates a NEW version and demotes the old to PREVIOUS.
 *  - Orphan: a document referenced by nothing is flagged candidateForDeletion.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(DocumentService.class)
class DocumentServiceTest {

    @Autowired DocumentService service;
    @Autowired DocumentRepository docs;
    @Autowired DocumentVersionRepository versions;
    @Autowired BookDocumentRepository bookDocs;
    @Autowired BookRepository bookRepo;
    @Autowired SubjectRepository subjectRepo;
    @Autowired ClassRepository classRepo;

    private Document register(String hash, String name) {
        return service.registerOrGet(hash, name, "application/pdf", 1000L,
                "https://src/" + name, "https://official/" + name, null,
                "SUPABASE", "documents/" + name, true, "GRANTED", "INTERNAL",
                "2026-27", "HOST", "etag-" + hash);
    }

    @Test
    void duplicateContentStoresOneDocumentWithTwoReferences() {
        Document first = register("hashABC", "math.pdf");
        Document second = register("hashABC", "mathematics-copy.pdf"); // same bytes, different name

        assertEquals(first.getId(), second.getId(), "Same checksum must dedup to one document");
        assertEquals(1, docs.findAll().size(), "Only one document row expected");

        UUID bookA = createBook("book-a");
        UUID bookB = createBook("book-b");
        service.linkToBook(bookA, first.getId(), "TEXTBOOK");
        service.linkToBook(bookB, first.getId(), "TEXTBOOK");

        assertEquals(2, bookDocs.countByDocumentId(first.getId()),
                "One stored document, referenced by two books");
    }

    @Test
    void changedContentCreatesNewVersionAndDemotesPrevious() {
        Document d = register("hash2025", "science.pdf");
        assertEquals(1, d.getVersion());

        boolean created = service.recordNewVersionIfChanged(d.getId(), "hash2026", "etag2026",
                2048L, "SUPABASE", "documents/science-2026.pdf",
                "https://src/science-2026.pdf", "2026-27");
        assertTrue(created, "Different checksum must create a new version");

        Document reloaded = docs.findById(d.getId()).orElseThrow();
        assertEquals(2, reloaded.getVersion());
        assertEquals("hash2026", reloaded.getDocumentHash());

        List<DocumentVersion> vs = versions.findByDocumentIdOrderByVersionDesc(d.getId());
        assertEquals(2, vs.size());
        assertEquals("CURRENT", vs.get(0).getRetention());   // version 2
        assertEquals("PREVIOUS", vs.get(1).getRetention());  // version 1 demoted
    }

    @Test
    void identicalChecksumDoesNotCreateNewVersion() {
        Document d = register("sameHash", "english.pdf");
        boolean created = service.recordNewVersionIfChanged(d.getId(), "sameHash", "etag-x",
                1000L, "SUPABASE", "documents/english.pdf", "https://src/english.pdf", "2026-27");
        assertFalse(created, "Identical content must NOT create a new version");
        assertEquals(1, versions.findByDocumentIdOrderByVersionDesc(d.getId()).size());
    }

    @Test
    void unreferencedDocumentIsFlaggedAsOrphan() {
        Document orphan = register("orphanHash", "orphan.pdf");
        Document referenced = register("refHash", "referenced.pdf");
        UUID book = createBook("book-ref");
        service.linkToBook(book, referenced.getId(), "TEXTBOOK");

        service.flagOrphans();

        assertTrue(docs.findById(orphan.getId()).orElseThrow().isCandidateForDeletion(),
                "Unreferenced document must be a deletion candidate");
        assertFalse(docs.findById(referenced.getId()).orElseThrow().isCandidateForDeletion(),
                "Referenced document must NOT be a deletion candidate");
    }

    // --- helper: minimal class/subject/book graph so FK constraints are satisfied ---
    private UUID createBook(String slug) {
        ClassEntity cls = new ClassEntity();
        cls.setCode("9-" + slug);
        cls.setName("Class IX");
        cls.setActive(true);
        cls.setDisplayOrder(1);
        cls = classRepo.save(cls);

        Subject subj = new Subject();
        subj.setClassId(cls.getId());
        subj.setCode("subj-" + slug);
        subj.setName("Subject");
        subj.setDisplayOrder(1);
        subj = subjectRepo.save(subj);

        Book b = new Book();
        b.setSubjectId(subj.getId());
        b.setSlug(slug);
        b.setTitle("Book " + slug);
        b = bookRepo.save(b);
        return b.getId();
    }
}
