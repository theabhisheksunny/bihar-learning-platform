-- Storage optimization + PDF architecture.
-- Principle (docs section 1, 44): PostgreSQL stores metadata + references + checksums + versions.
-- Binaries live in object storage (Supabase Storage) only when legally permitted. The source +
-- content + version metadata are the source of truth, NOT the bucket or the DB.

-- ---------------------------------------------------------------------------
-- Extend content_sources with the provenance fields from docs section 4.
-- (The base table was created in V1; add the new columns idempotently.)
-- ---------------------------------------------------------------------------
ALTER TABLE content_sources ADD COLUMN IF NOT EXISTS source_type           TEXT;   -- GOVERNMENT, EDUCATIONAL, THIRD_PARTY
ALTER TABLE content_sources ADD COLUMN IF NOT EXISTS authority_level        TEXT;   -- OFFICIAL, VERIFIED, UNVERIFIED
ALTER TABLE content_sources ADD COLUMN IF NOT EXISTS license                TEXT;
ALTER TABLE content_sources ADD COLUMN IF NOT EXISTS terms_url              TEXT;
ALTER TABLE content_sources ADD COLUMN IF NOT EXISTS robots_status          TEXT;   -- UNKNOWN, ALLOWED, DISALLOWED
ALTER TABLE content_sources ADD COLUMN IF NOT EXISTS api_available          BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE content_sources ADD COLUMN IF NOT EXISTS api_documentation_url  TEXT;
ALTER TABLE content_sources ADD COLUMN IF NOT EXISTS status                 TEXT NOT NULL DEFAULT 'ACTIVE';

-- Seed the new descriptive fields for the known sources (honest, from research; UNKNOWN where unverified).
UPDATE content_sources SET source_type='GOVERNMENT',  authority_level='OFFICIAL',   license='CC BY-NC-ND 4.0', robots_status='UNKNOWN' WHERE source_key='NCERT';
UPDATE content_sources SET source_type='GOVERNMENT',  authority_level='OFFICIAL',   robots_status='UNKNOWN'                              WHERE source_key='BEP_ELOTS';
UPDATE content_sources SET source_type='GOVERNMENT',  authority_level='OFFICIAL',   robots_status='UNKNOWN'                              WHERE source_key='SCERT';
UPDATE content_sources SET source_type='GOVERNMENT',  authority_level='VERIFIED',   license='CC BY-NC-ND / CC BY-NC-SA', robots_status='UNKNOWN' WHERE source_key='DIKSHA';
UPDATE content_sources SET source_type='THIRD_PARTY', authority_level='UNVERIFIED', robots_status='UNKNOWN'                              WHERE source_key='THIRD_PARTY';

-- ---------------------------------------------------------------------------
-- documents: canonical file metadata. The binary is NOT stored here.
-- Deduplication is by checksum (document_hash), enforced with a UNIQUE index.
-- ---------------------------------------------------------------------------
CREATE TABLE documents (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_hash        TEXT NOT NULL,                 -- SHA-256 hex; canonical identity (docs 6,7)
    file_name            TEXT,
    original_file_name   TEXT,
    mime_type            TEXT,
    file_size            BIGINT,
    etag                 TEXT,
    source_url           TEXT,
    official_source_url  TEXT,
    source_id            UUID REFERENCES content_sources(id) ON DELETE SET NULL,
    -- Where the current representation lives:
    storage_provider     TEXT NOT NULL DEFAULT 'EXTERNAL',  -- SUPABASE, EXTERNAL, ...
    storage_path         TEXT,                              -- object key when hosted; null when external
    is_hosted            BOOLEAN NOT NULL DEFAULT FALSE,     -- true only when we legally re-host the binary
    hosting_permission   TEXT NOT NULL DEFAULT 'UNKNOWN',    -- GRANTED, DENIED, UNKNOWN (docs 36)
    access_mode          TEXT NOT NULL DEFAULT 'EXTERNAL',   -- INTERNAL, EXTERNAL, EMBEDDED, DOWNLOAD, STREAM (docs 35)
    academic_year        TEXT,
    language             TEXT,
    medium               TEXT,
    version              INT NOT NULL DEFAULT 1,             -- current version number
    status               TEXT NOT NULL DEFAULT 'PENDING_REVIEW', -- HOST, EXTERNAL_REFERENCE, REJECT, PENDING_REVIEW, FAILED
    candidate_for_deletion BOOLEAN NOT NULL DEFAULT FALSE,   -- cleanup review flag (docs 21)
    -- download/retry bookkeeping (docs 25)
    attempt_count        INT NOT NULL DEFAULT 0,
    last_attempt_at      TIMESTAMPTZ,
    next_retry_at        TIMESTAMPTZ,
    error_message        TEXT,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_verified_at     TIMESTAMPTZ
);
-- Deduplication: identical content is stored once (docs 6,7). Different filenames, same bytes =>
-- same document.
CREATE UNIQUE INDEX uq_documents_hash ON documents(document_hash);
CREATE INDEX idx_documents_source ON documents(source_id);
CREATE INDEX idx_documents_status ON documents(status);
CREATE INDEX idx_documents_candidate_del ON documents(candidate_for_deletion);

-- ---------------------------------------------------------------------------
-- document_versions: history of a document's content over time (docs 8, 9).
-- A new row is created only when the CONTENT genuinely changes (checksum differs), not merely
-- because a source URL changed.
-- ---------------------------------------------------------------------------
CREATE TABLE document_versions (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id       UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    version           INT NOT NULL,
    document_hash     TEXT NOT NULL,
    etag              TEXT,
    last_modified     TEXT,
    file_size         BIGINT,
    storage_provider  TEXT,
    storage_path      TEXT,
    source_url        TEXT,
    academic_year     TEXT,
    retention         TEXT NOT NULL DEFAULT 'CURRENT',  -- CURRENT, PREVIOUS, ARCHIVED (docs 9)
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_document_versions_doc ON document_versions(document_id);
CREATE UNIQUE INDEX uq_document_versions_doc_version ON document_versions(document_id, version);

-- ---------------------------------------------------------------------------
-- Reusable relationship: many books/chapters can reference the SAME document (docs 6).
-- ---------------------------------------------------------------------------
CREATE TABLE book_documents (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id     UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    role        TEXT NOT NULL DEFAULT 'TEXTBOOK',  -- TEXTBOOK, SUPPLEMENT, COVER_IMAGE, ...
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (book_id, document_id, role)
);
CREATE INDEX idx_book_documents_book ON book_documents(book_id);
CREATE INDEX idx_book_documents_doc ON book_documents(document_id);

CREATE TABLE chapter_documents (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chapter_id  UUID NOT NULL REFERENCES chapters(id) ON DELETE CASCADE,
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    role        TEXT NOT NULL DEFAULT 'CHAPTER_PDF',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (chapter_id, document_id, role)
);
CREATE INDEX idx_chapter_documents_chapter ON chapter_documents(chapter_id);
CREATE INDEX idx_chapter_documents_doc ON chapter_documents(document_id);

-- ---------------------------------------------------------------------------
-- Images use the SAME dedup-by-checksum strategy (docs 17, 18). Binary not stored in PG.
-- ---------------------------------------------------------------------------
CREATE TABLE images (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    image_hash       TEXT NOT NULL,
    mime_type        TEXT,
    file_size        BIGINT,
    width            INT,
    height           INT,
    variant          TEXT,     -- thumbnail, card, desktop, mobile (docs 17)
    storage_provider TEXT NOT NULL DEFAULT 'EXTERNAL',
    storage_path     TEXT,
    source_url       TEXT,
    is_hosted        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX uq_images_hash_variant ON images(image_hash, variant);
