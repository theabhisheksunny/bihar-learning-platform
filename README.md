# Bihar Class IX Student Learning Platform

A single, student-friendly destination for **Bihar Board Class 9** learning material: syllabus,
books, chapters, summaries, questions, answers and PDFs — with the **source shown on every page**.

> Built API-first and content-honest. Nothing is fabricated: unverified fields are left empty,
> sample content is clearly labelled, AI aids are never presented as official textbook content.
> See [`docs/content-policy.md`](docs/content-policy.md).

- **Frontend:** Next.js 14 (App Router) · TypeScript · Tailwind · pdf.js reader
- **Backend:** Java 21 · Spring Boot 3 (Web, Data JPA, Security, Validation, Actuator) · Flyway
- **Data:** PostgreSQL 16 (+ `unaccent`) · Redis 7 · PostgreSQL full-text search
- **Infra:** Docker + docker-compose (local) · Kubernetes-compatible manifests

---

## 1. What's inside

```
Student Learning Platform/
├── backend/            Spring Boot API, entities, services, source adapters, sync engine, Flyway
├── frontend/           Next.js app (pages, components, PDF reader, admin UI, Playwright E2E)
├── infrastructure/     nginx reverse proxy + Kubernetes manifests
├── docs/               research, requirements, architecture, data model, API, content policy
├── docker-compose.yml  local dev stack
└── .env.example        environment template
```

Documentation to read first:
- [`docs/source-research.md`](docs/source-research.md) — what sources exist, licensing, what is `UNVERIFIED`
- [`docs/content-policy.md`](docs/content-policy.md) — licensing + "no fake data" rules
- [`docs/requirements.md`](docs/requirements.md) · [`docs/architecture.md`](docs/architecture.md) · [`docs/data-model.md`](docs/data-model.md) · [`docs/api.md`](docs/api.md)

---

## 2. Run locally — the easy way (Docker)

Prerequisites: Docker + Docker Compose.

```bash
cp .env.example .env        # review values; change secrets for anything non-local
docker compose up --build
```

This starts PostgreSQL, Redis, the backend (which runs Flyway migrations + seeds on first boot),
and the frontend.

- Frontend: http://localhost:3000
- API + Swagger UI: http://localhost:8080/swagger-ui.html
- Admin panel: http://localhost:3000/admin (dev login below)

Stop with `docker compose down` (add `-v` to also drop the database volume).

---

## 3. Run locally — the manual way

### 3a. Start PostgreSQL and Redis
Use Docker for just the datastores:
```bash
docker run -d --name bihar-postgres -e POSTGRES_DB=bihar_learning -e POSTGRES_USER=bihar \
  -e POSTGRES_PASSWORD=bihar -p 5432:5432 postgres:16-alpine
docker run -d --name bihar-redis -p 6379:6379 redis:7-alpine
```
(Or point the backend at your own PostgreSQL/Redis via env vars — see §7.)

> No Redis? Set `CACHE_TYPE=simple` and the backend uses an in-memory cache instead.

### 3b. Start the backend (runs migrations + seed automatically)
```bash
cd backend
mvn spring-boot:run
```
Flyway applies `V1__init_schema.sql` (schema + indexes) and `V2__seed_demo_data.sql`
(classes, sources, Class IX subjects, and clearly-labelled DEMO books/chapters) on startup.

API is on http://localhost:8080. Health: http://localhost:8080/actuator/health.

### 3c. Start the frontend
```bash
cd frontend
npm install
npm run dev          # http://localhost:3000
```
The frontend proxies `/api/*` to the backend (`BACKEND_URL`), so the browser never calls any
external government site directly.

---

## 4. Migrations and seed/import

- **Migrations:** Flyway runs automatically on backend startup. Files live in
  `backend/src/main/resources/db/migration`. Add new changes as `V3__...sql`, `V4__...sql`, etc.
- **Seed:** `V2__seed_demo_data.sql` inserts configuration (classes 6–12, five content sources)
  and **DEMO** Class IX content that uses only **real, observed** source URLs and `null` for every
  unverified field. Promote content to `OFFICIAL` through the admin review flow after an operator
  confirms each source's terms.
- **Real content import:** happens through the **sync engine** (admin-triggered) once a source's
  robots.txt/terms are confirmed and the source is enabled — see §6.

---

## 4a. Storage & PDF architecture (storage-optimized, provider-independent)

PDF/image **binaries are never stored in PostgreSQL**. The database holds only metadata,
references, checksums, versions, permissions and source provenance. Binaries live in object
storage **only when legally permitted**; otherwise the platform keeps metadata + the official
source URL and links out.

**Storage abstraction (provider-independent).** Business logic talks to `FileStorageService`, not
to any concrete provider. Switching providers later (S3, R2, GCS, Azure) is a configuration change,
not a rewrite.

```
FileStorageService
  └── FileStorageProvider (SPI)
        ├── SupabaseStorageProvider   (STORAGE_PROVIDER=SUPABASE — hosts permitted files)
        └── ExternalUrlProvider       (STORAGE_PROVIDER=EXTERNAL — default; links only, never mirrors)
```

**Model (source of truth = metadata, not the bucket):**
`Book → Document → DocumentVersion → storage provider / official URL`
(`documents`, `document_versions`, `book_documents`, `chapter_documents`, `images` — see
`V3__document_storage.sql`).

**Pipeline (`IngestionService`, docs §5/§23/§26/§40):**
```
discover → identify source → check permission/license → conditional fetch (HEAD/ETag) →
download only when required → validate PDF (signature, content-type, size) → SHA-256 checksum →
dedup / version → store in object storage IF permitted → persist metadata in PostgreSQL
```

Key behaviours:
- **Deduplication** by SHA-256 with a `UNIQUE` index on `documents.document_hash`. Identical bytes
  with different filenames resolve to **one** document referenced by many books/chapters.
- **Versioning** only when content genuinely changes (checksum differs) — not when a URL changes.
  Previous versions are retained as `PREVIOUS`/`ARCHIVED` (configurable retention).
- **Legal safety default:** if hosting permission is not clearly `GRANTED`, the document is kept as
  an **external reference** (metadata + official URL), never auto-mirrored.
- **Large files stream** through the checksum/upload; they are not buffered whole into the DB.
- **Conditional fetch** skips re-downloading unchanged sources (ETag / Last-Modified / size).
- **Failure handling:** download failures use a retry/backoff lifecycle (`RETRYING` → `FAILED`
  after `SYNC_MAX_ATTEMPTS`); validation rejects are permanent `FAILED` (no retry).

**Serving (bandwidth-efficient).** Hosted, distributable PDFs are served via a direct storage/CDN
URL (signed or public) so the browser fetches them directly — Spring Boot does not proxy the bytes.
The resolution endpoint sets short cache headers; versioned document URLs are immutable.

**Admin storage dashboard** (`/admin`, `ROLE_ADMIN`): usage (used/PDFs/images/archived/external/
failed), a documents table, orphaned-file candidates and failed files. Usage/quota are reported as
`UNKNOWN` unless `STORAGE_QUOTA_BYTES` is configured — the platform never invents storage figures.

**Relevant environment variables** (all optional; safe defaults keep everything in `EXTERNAL`
mode so no bucket is required to run):

| Variable | Purpose | Default |
|----------|---------|---------|
| `STORAGE_PROVIDER` | `SUPABASE` or `EXTERNAL` | `EXTERNAL` |
| `STORAGE_BUCKET` | object-storage bucket name | `documents` |
| `SUPABASE_URL` / `SUPABASE_SERVICE_KEY` | Supabase Storage (server-side only) | empty |
| `STORAGE_MAX_HOST_BYTES` | max file size to host (else external) | `52428800` (50 MB) |
| `STORAGE_SIGNED_URL_EXPIRY` | signed URL lifetime (s) | `3600` |
| `STORAGE_QUOTA_BYTES` | quota for dashboard thresholds (`0` = UNKNOWN) | `0` |
| `STORAGE_WARN_PERCENT` / `STORAGE_CRITICAL_PERCENT` | threshold levels | `70` / `85` |

---

## 5. Tests

### Backend (JUnit + Mockito + H2)
```bash
cd backend
mvn test
```
Covers: a web-layer slice test for the API envelope, PDF-resolution unit tests, a JPA
integration test (Class → Subject → Book → Chapter) on an in-memory PostgreSQL-mode database,
and the **storage architecture** suite — SHA-256 dedup identity, streaming-checksum for large
files, PDF validation (rejecting HTML-as-PDF), the storage decision engine, document
deduplication/versioning/orphan detection against a real DB, and the end-to-end ingestion
pipeline (discover → permission → validate → checksum → dedup/version → store → persist →
associate with a book). `mvn test` currently runs **29 tests, 0 failures**.

### Frontend E2E (Playwright)
```bash
cd frontend
npx playwright install        # first time only (downloads browsers)
npm run build                 # the config starts `npm run start`
npm run test:e2e              # or: npx playwright test
```
- `home.spec.ts` — shell smoke tests (hero, skip link, search nav, 404). No backend needed.
- `student-journey.spec.ts` — Home → Class IX → subject → book → chapter → read/summary →
  questions → search. **Skips automatically** if the backend/seed is not running.

To run the full journey, bring the whole stack up first (`docker compose up`), then run the E2E.

---

## 6. Content synchronization

Sync is **disabled by default** (`SYNC_ENABLED=false`) and additionally per-source until an
operator confirms that source's `robots.txt`/terms.

To enable (only after confirming compliance):
1. Set `SYNC_ENABLED=true` in the environment.
2. In the DB (or admin), set the source's `enabled=true` and `robots_confirmed=true`.
3. In the admin panel → **Content sources** → **Sync Now**.

The engine discovers items via the source's adapter, records each as `PENDING_REVIEW` in the
change log, and **never auto-overwrites** official content. An admin approves changes to publish.
Adapters are polite: descriptive User-Agent, rate limiting, retry/backoff; they never bypass
authentication, CAPTCHAs, paywalls, or DRM.

---

## 7. Environment variables

Copy `.env.example` to `.env`. Key variables:

| Variable | Purpose | Default (dev) |
|----------|---------|---------------|
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | PostgreSQL connection | local postgres |
| `REDIS_HOST` / `REDIS_PORT` / `CACHE_TYPE` | cache (`redis` or `simple`) | localhost / redis |
| `CORS_ALLOWED_ORIGINS` | allowed frontend origin(s) | http://localhost:3000 |
| `JWT_SECRET` | Base64 256-bit JWT signing key **(change in prod)** | dev placeholder |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | bootstrap admin **(change in prod)** | admin@bihar-learning.local / admin12345 |
| `SYNC_ENABLED` | master switch for live crawling | `false` |
| `AI_ENABLED` | AI learning-aid generation | `false` |
| `BACKEND_URL` | frontend → backend proxy target | http://localhost:8080 |
| `SITE_URL` | canonical site URL (SEO/sitemap) | http://localhost:3000 |

**Never** commit real secrets or put them in frontend code.

---

## 8. Admin

- Sign in at `/admin`. Dev credentials: `admin@bihar-learning.local` / `admin12345` (override via
  `ADMIN_EMAIL` / `ADMIN_PASSWORD`; a bootstrap admin is created on first run).
- Dashboard shows counts (books/subjects/chapters/sources/pending), content sources with
  **Sync Now**, a **review queue** (approve/reject), and recent sync jobs.
- Admin APIs require a JWT with `ROLE_ADMIN` (see `docs/api.md`).

---

## 9. Known limitations (honest)

- **No public API** was found for Bihar/NCERT Class IX content; it is published as static PDFs.
  The platform stores metadata + links and streams/opens the official document.
- **`UNVERIFIED` fields:** exact live PDF URL templates, the complete Class IX subject catalogue,
  each portal's `robots.txt`/terms, and the DIKSHA API access model were **not** confirmable from
  the research environment (some government sites reset automated connections). An operator must
  confirm these in a browser before enabling live sync. Seed content is labelled `DEMO` /
  `PENDING_REVIEW` accordingly.
- **PDF delivery** defaults to *link/open at official source* (CC BY-NC-ND is No-Derivatives,
  Non-Commercial). Unmodified proxying is gated per source behind an admin decision.
- **AI learning aids** and **text-to-speech** are architected (pluggable) but off in v1.
- **Full WCAG conformance** needs manual assistive-technology testing and expert review; the app
  implements the structural basics (semantic HTML, keyboard nav, focus, ARIA on tabs/PDF, alt
  text, contrast) but automated checks alone don't prove full compliance.
- Local tests ran on JDK 25 / Node 24; the backend targets Java 21 (Byte Buddy experimental flag
  is enabled for Mockito on newer JVMs).

---

## 10. Future roadmap

- Confirm source terms + live PDF templates; enable `NcertSourceAdapter` / `BepELotsSourceAdapter`
  / `ScertBiharSourceAdapter` and promote verified content to `OFFICIAL`.
- Add `DikshaSourceAdapter` once its API access model is confirmed.
- Enable grounded, reviewed AI summaries/questions (clearly labelled) and a real TTS provider.
- Expand to Classes VI–XII — primarily a content/config task (add `classes`/`subjects` rows and,
  if needed, register a new adapter bean); the frontend renders classes/subjects generically.
- Move search to OpenSearch/Elasticsearch if PostgreSQL FTS becomes a bottleneck.
- Add observability (Prometheus/Grafana/Loki), bookmarks/progress for signed-in students, and PDF
  text extraction/OCR to improve search and chapter mapping.

---

## 11. Content & licensing note

Content belongs to its publishers (NCERT, Bihar Education Department, SCERT, DIKSHA). NCERT
textbooks are **CC BY-NC-ND**. This project shows the source on every page, links to the official
document, and does not present modified or AI-generated text as official content. This is an
educational project and **not** an official government portal.
