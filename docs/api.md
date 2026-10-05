# API — Bihar Class IX Student Learning Platform

**Status:** Phase 3 deliverable. Live OpenAPI/Swagger served by the backend at
`/swagger-ui.html` and `/v3/api-docs` (dev/admin). All endpoints are versioned under `/api/v1`.

## Conventions

- **Base:** `/api/v1`
- **Response envelope (success):**
  ```json
  { "success": true, "data": { } , "meta": { } }
  ```
- **Response envelope (error):**
  ```json
  { "success": false, "error": { "code": "BOOK_NOT_FOUND", "message": "Book could not be found" } }
  ```
- **Pagination:** `?page=0&size=20`; `meta` carries `page,size,totalElements,totalPages`.
- No stack traces exposed. Validation errors use code `VALIDATION_ERROR` with field details.

## Public (student) endpoints

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/classes` | List classes (active + coming-soon) |
| GET | `/classes/{classId}` | Class detail |
| GET | `/classes/{classId}/subjects` | Subjects for a class (with counts) |
| GET | `/subjects/{subjectId}` | Subject detail |
| GET | `/subjects/{subjectId}/books` | Books for a subject (paginated) |
| GET | `/books/{bookId}` | Book detail incl. provenance |
| GET | `/books/{bookId}/chapters` | Chapters of a book (ordered) |
| GET | `/chapters/{chapterId}` | Chapter detail (all sections' availability) |
| GET | `/chapters/{chapterId}/summary` | Summary content (labelled if AI) |
| GET | `/chapters/{chapterId}/questions?category=` | Questions by category |
| GET | `/chapters/{chapterId}/answers` | Answers |
| GET | `/chapters/{chapterId}/mcqs` | MCQs |
| GET | `/syllabus/{classId}?subjectId=` | Syllabus |
| GET | `/search?q=&type=&page=&size=` | Global search |
| GET | `/content/{contentId}` | Generic content fetch by id |
| GET | `/content/{contentId}/pdf` | Resolve PDF → 302 deep link or unmodified proxy stream |

`{...Id}` accepts a UUID or a human slug where applicable (books/chapters resolve slugs).

### Example — book detail
`GET /api/v1/books/bihar-ncert-class-9-mathematics`
```json
{
  "success": true,
  "data": {
    "id": "…", "slug": "bihar-ncert-class-9-mathematics",
    "title": "Mathematics", "subject": "Mathematics", "class": "Class IX",
    "board": null, "publisher": "NCERT", "language": "en",
    "academicYear": "2026-27",
    "pdfAvailable": true, "downloadAllowed": false,
    "provenance": {
      "sourceName": "NCERT", "sourceUrl": "https://ncert.nic.in/textbook.php",
      "license": "CC BY-NC-ND 4.0", "verificationStatus": "PENDING_REVIEW"
    },
    "chapterCount": 12
  }
}
```
Fields that are unverified are `null` — never invented.

## Admin endpoints (require `ROLE_ADMIN`, JWT bearer)

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/admin/auth/login` | Obtain JWT |
| GET | `/admin/sources` | List content sources + sync status |
| POST | `/admin/sources/{id}/sync` | Trigger sync (if enabled + robots confirmed) |
| GET | `/admin/sync-jobs` | Sync job history |
| GET | `/admin/sync-jobs/{id}` | Job detail + logs |
| GET | `/admin/review` | Pending content change queue |
| POST | `/admin/review/{changeId}/approve` | Approve → promote content |
| POST | `/admin/review/{changeId}/reject` | Reject change |
| POST | `/admin/content/{id}/outdated` | Mark content outdated |
| GET | `/admin/content/{id}/versions` | Version history |
| GET | `/admin/ai-content` | AI content review queue |
| POST | `/admin/ai-content/{id}/approve` | Approve AI aid (→ VERIFIED, never OFFICIAL) |
| GET | `/admin/stats` | Counts: books/subjects/chapters/sources/last-sync/pending |

## Error codes

`VALIDATION_ERROR`, `CLASS_NOT_FOUND`, `SUBJECT_NOT_FOUND`, `BOOK_NOT_FOUND`,
`CHAPTER_NOT_FOUND`, `CONTENT_NOT_FOUND`, `SYLLABUS_NOT_FOUND`, `PDF_SOURCE_UNAVAILABLE`,
`SOURCE_DISABLED`, `ROBOTS_NOT_CONFIRMED`, `UNAUTHORIZED`, `FORBIDDEN`, `RATE_LIMITED`,
`INTERNAL_ERROR`.

## Security

- Public GETs are open (no login to read).
- `/admin/**` requires JWT + `ROLE_ADMIN`.
- Rate limiting, CORS allowlist, secure headers, input validation on all write endpoints.
- `/content/{id}/pdf` enforces per-source license policy (link vs proxy) and never exposes
  external credentials.
