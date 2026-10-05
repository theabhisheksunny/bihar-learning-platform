-- Bihar Class IX Student Learning Platform - initial schema
-- PostgreSQL 16. UUID PKs. Provenance embedded on content-bearing tables.

CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "unaccent";

-- unaccent() is only STABLE (it depends on a dictionary), so Postgres rejects it inside an
-- index expression ("functions in index expression must be marked IMMUTABLE"). Wrap it in an
-- IMMUTABLE SQL function so it can be used in GIN full-text indexes. The same wrapper MUST be
-- used in queries (see SearchRepository) so the index is actually used.
CREATE OR REPLACE FUNCTION f_unaccent(text)
    RETURNS text
    LANGUAGE sql
    IMMUTABLE
    PARALLEL SAFE
    STRICT
AS $$
    SELECT public.unaccent('public.unaccent'::regdictionary, $1)
$$;

-- ---------------------------------------------------------------------------
-- Reference / catalogue
-- ---------------------------------------------------------------------------
CREATE TABLE classes (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code          TEXT NOT NULL UNIQUE,
    name          TEXT NOT NULL,
    display_order INT  NOT NULL DEFAULT 0,
    is_active     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE content_sources (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_key       TEXT NOT NULL UNIQUE,
    name             TEXT NOT NULL,
    authority_tier   INT  NOT NULL DEFAULT 1,
    base_url         TEXT,
    adapter_bean     TEXT,
    enabled          BOOLEAN NOT NULL DEFAULT FALSE,
    robots_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    license_default  TEXT,
    last_checked_at  TIMESTAMPTZ,
    last_success_at  TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE subjects (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_id      UUID NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    code          TEXT NOT NULL,
    name          TEXT NOT NULL,
    name_hi       TEXT,
    icon          TEXT,
    language      TEXT NOT NULL DEFAULT 'en',
    medium        TEXT,
    academic_year TEXT,
    display_order INT  NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (class_id, code)
);
CREATE INDEX idx_subjects_class ON subjects(class_id);

-- ---------------------------------------------------------------------------
-- Books + chapters (with provenance columns)
-- ---------------------------------------------------------------------------
CREATE TABLE books (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subject_id          UUID NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    source_id           UUID REFERENCES content_sources(id) ON DELETE SET NULL,
    slug                TEXT NOT NULL UNIQUE,
    title               TEXT NOT NULL,
    subtitle            TEXT,
    board               TEXT,
    publisher           TEXT,
    language            TEXT NOT NULL DEFAULT 'en',
    medium              TEXT,
    edition             TEXT,
    isbn                TEXT,
    cover_image         TEXT,
    pdf_url             TEXT,
    official_source_url TEXT,
    description         TEXT,
    chapter_count       INT NOT NULL DEFAULT 0,
    download_allowed    BOOLEAN NOT NULL DEFAULT FALSE,
    status              TEXT NOT NULL DEFAULT 'DRAFT',
    last_updated        TIMESTAMPTZ,
    -- provenance
    source_type         TEXT,
    source_name         TEXT,
    source_url          TEXT,
    source_document_url TEXT,
    retrieved_at        TIMESTAMPTZ,
    published_at        TIMESTAMPTZ,
    academic_year       TEXT,
    version             INT NOT NULL DEFAULT 1,
    checksum            TEXT,
    license             TEXT,
    verification_status TEXT NOT NULL DEFAULT 'PENDING_REVIEW',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_books_subject ON books(subject_id);
CREATE INDEX idx_books_slug ON books(slug);
CREATE INDEX idx_books_verification ON books(verification_status);
CREATE INDEX idx_books_year ON books(academic_year);

CREATE TABLE book_versions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id     UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    version     INT  NOT NULL,
    snapshot    JSONB NOT NULL,
    change_type TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_book_versions_book ON book_versions(book_id);

CREATE TABLE chapters (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id             UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    chapter_number      INT NOT NULL,
    slug                TEXT NOT NULL,
    title               TEXT NOT NULL,
    title_hi            TEXT,
    description         TEXT,
    source_page_start   INT,
    source_page_end     INT,
    pdf_reference       TEXT,
    -- provenance
    source_type         TEXT,
    source_name         TEXT,
    source_url          TEXT,
    source_document_url TEXT,
    retrieved_at        TIMESTAMPTZ,
    published_at        TIMESTAMPTZ,
    academic_year       TEXT,
    version             INT NOT NULL DEFAULT 1,
    checksum            TEXT,
    license             TEXT,
    verification_status TEXT NOT NULL DEFAULT 'PENDING_REVIEW',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (book_id, chapter_number)
);
CREATE INDEX idx_chapters_book ON chapters(book_id);
CREATE INDEX idx_chapters_slug ON chapters(slug);

-- ---------------------------------------------------------------------------
-- Chapter learning content + practice
-- ---------------------------------------------------------------------------
CREATE TABLE chapter_contents (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chapter_id          UUID NOT NULL REFERENCES chapters(id) ON DELETE CASCADE,
    content_type        TEXT NOT NULL, -- SUMMARY, KEY_CONCEPTS, DEFINITIONS, IMPORTANT_POINTS, REVISION, FORMULAS
    body                TEXT NOT NULL,
    language            TEXT NOT NULL DEFAULT 'en',
    is_ai               BOOLEAN NOT NULL DEFAULT FALSE,
    -- ai metadata
    ai_generated_at     TIMESTAMPTZ,
    ai_model            TEXT,
    ai_source_content_id UUID,
    ai_prompt_version   TEXT,
    ai_review_status    TEXT,
    -- provenance
    source_type         TEXT,
    source_name         TEXT,
    source_url          TEXT,
    source_document_url TEXT,
    retrieved_at        TIMESTAMPTZ,
    published_at        TIMESTAMPTZ,
    academic_year       TEXT,
    version             INT NOT NULL DEFAULT 1,
    checksum            TEXT,
    license             TEXT,
    verification_status TEXT NOT NULL DEFAULT 'PENDING_REVIEW',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_chapter_contents_chapter ON chapter_contents(chapter_id);
CREATE INDEX idx_chapter_contents_type ON chapter_contents(chapter_id, content_type);

CREATE TABLE questions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chapter_id          UUID NOT NULL REFERENCES chapters(id) ON DELETE CASCADE,
    category            TEXT NOT NULL, -- MCQ, VERY_SHORT, SHORT, LONG, IMPORTANT, PREVIOUS_EXAM
    text                TEXT NOT NULL,
    marks               INT,
    difficulty          TEXT,
    is_ai               BOOLEAN NOT NULL DEFAULT FALSE,
    source_type         TEXT,
    source_name         TEXT,
    source_url          TEXT,
    academic_year       TEXT,
    version             INT NOT NULL DEFAULT 1,
    license             TEXT,
    verification_status TEXT NOT NULL DEFAULT 'PENDING_REVIEW',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_questions_chapter_cat ON questions(chapter_id, category);

CREATE TABLE answers (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id         UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    text                TEXT NOT NULL,
    is_ai               BOOLEAN NOT NULL DEFAULT FALSE,
    source_type         TEXT,
    source_name         TEXT,
    verification_status TEXT NOT NULL DEFAULT 'PENDING_REVIEW',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_answers_question ON answers(question_id);

CREATE TABLE mcqs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chapter_id          UUID NOT NULL REFERENCES chapters(id) ON DELETE CASCADE,
    question            TEXT NOT NULL,
    options             JSONB NOT NULL,
    correct_index       INT NOT NULL,
    explanation         TEXT,
    is_ai               BOOLEAN NOT NULL DEFAULT FALSE,
    source_type         TEXT,
    source_name         TEXT,
    verification_status TEXT NOT NULL DEFAULT 'PENDING_REVIEW',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_mcqs_chapter ON mcqs(chapter_id);

CREATE TABLE exercise_solutions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chapter_id          UUID NOT NULL REFERENCES chapters(id) ON DELETE CASCADE,
    exercise_ref        TEXT NOT NULL,
    problem             TEXT,
    solution            TEXT NOT NULL,
    is_ai               BOOLEAN NOT NULL DEFAULT FALSE,
    source_type         TEXT,
    source_name         TEXT,
    verification_status TEXT NOT NULL DEFAULT 'PENDING_REVIEW',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_exercise_solutions_chapter ON exercise_solutions(chapter_id);

CREATE TABLE syllabus (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_id            UUID NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    subject_id          UUID REFERENCES subjects(id) ON DELETE CASCADE,
    title               TEXT NOT NULL,
    body                TEXT,
    document_url        TEXT,
    source_type         TEXT,
    source_name         TEXT,
    source_url          TEXT,
    academic_year       TEXT,
    license             TEXT,
    verification_status TEXT NOT NULL DEFAULT 'PENDING_REVIEW',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_syllabus_class_subject ON syllabus(class_id, subject_id);

-- ---------------------------------------------------------------------------
-- Sync + change tracking
-- ---------------------------------------------------------------------------
CREATE TABLE sync_jobs (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_id          UUID NOT NULL REFERENCES content_sources(id) ON DELETE CASCADE,
    status             TEXT NOT NULL DEFAULT 'QUEUED',
    started_at         TIMESTAMPTZ,
    finished_at        TIMESTAMPTZ,
    records_discovered INT NOT NULL DEFAULT 0,
    records_updated    INT NOT NULL DEFAULT 0,
    records_failed     INT NOT NULL DEFAULT 0,
    triggered_by       TEXT,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_sync_jobs_source_status ON sync_jobs(source_id, status);

CREATE TABLE sync_logs (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id     UUID NOT NULL REFERENCES sync_jobs(id) ON DELETE CASCADE,
    level      TEXT NOT NULL DEFAULT 'INFO',
    message    TEXT NOT NULL,
    context    JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_sync_logs_job ON sync_logs(job_id);

CREATE TABLE content_change_log (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_id    UUID,
    content_table TEXT NOT NULL,
    source_id     UUID REFERENCES content_sources(id) ON DELETE SET NULL,
    old_version   INT,
    new_version   INT,
    change_type   TEXT NOT NULL, -- NEW, UPDATED, REMOVED, RENAMED
    detected_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    processed_at  TIMESTAMPTZ,
    status        TEXT NOT NULL DEFAULT 'PENDING_REVIEW',
    diff          JSONB
);
CREATE INDEX idx_change_log_status ON content_change_log(status);

-- ---------------------------------------------------------------------------
-- Users (minimal) + personalization
-- ---------------------------------------------------------------------------
CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         TEXT UNIQUE,
    display_name  TEXT,
    role          TEXT NOT NULL DEFAULT 'STUDENT',
    password_hash TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE bookmarks (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    book_id    UUID REFERENCES books(id) ON DELETE CASCADE,
    chapter_id UUID REFERENCES chapters(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_bookmarks_user ON bookmarks(user_id);

CREATE TABLE reading_progress (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    chapter_id UUID NOT NULL REFERENCES chapters(id) ON DELETE CASCADE,
    percent    INT NOT NULL DEFAULT 0,
    last_page  INT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, chapter_id)
);
CREATE INDEX idx_reading_progress_user ON reading_progress(user_id);

-- ---------------------------------------------------------------------------
-- Full-text search indexes (simple config + unaccent for multilingual-ish)
-- ---------------------------------------------------------------------------
CREATE INDEX idx_books_fts ON books
    USING GIN (to_tsvector('simple', f_unaccent(coalesce(title,'') || ' ' || coalesce(description,''))));
CREATE INDEX idx_chapters_fts ON chapters
    USING GIN (to_tsvector('simple', f_unaccent(coalesce(title,'') || ' ' || coalesce(description,''))));
CREATE INDEX idx_questions_fts ON questions
    USING GIN (to_tsvector('simple', f_unaccent(coalesce(text,''))));
CREATE INDEX idx_chapter_contents_fts ON chapter_contents
    USING GIN (to_tsvector('simple', f_unaccent(coalesce(body,''))));
