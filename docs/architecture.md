# Architecture — Bihar Class IX Student Learning Platform

**Status:** Phase 3 deliverable. See also `data-model.md`, `api.md`, `source-research.md`,
`content-policy.md`.

## 1. High-level system

```mermaid
flowchart TD
  Browser["Student / Admin Browser"]
  subgraph FE["Frontend — Next.js (TypeScript, React, Tailwind)"]
    Pages["App Router pages (SSR/SSG)"]
    APIClient["lib/api client"]
  end
  subgraph BE["Backend — Spring Boot (Java 21)"]
    Ctrl["REST Controllers /api/v1/*"]
    Svc["Services (content, search, sync, pdf, ai)"]
    Repo["Spring Data JPA repositories"]
    Adapters["Source Adapter layer"]
    Sched["Sync Scheduler"]
  end
  PG[("PostgreSQL")]
  Redis[("Redis cache")]
  subgraph EXT["External official sources"]
    NCERT["NCERT ncert.nic.in"]
    BEP["BEP e-LOTS bepclots.bihar.gov.in"]
    SCERT["SCERT Bihar scert.bihar.gov.in"]
    DIKSHA["DIKSHA (future)"]
  end

  Browser --> Pages
  Pages --> APIClient
  APIClient -->|HTTPS /api/v1| Ctrl
  Ctrl --> Svc
  Svc --> Repo
  Repo --> PG
  Svc --> Redis
  Svc --> Adapters
  Sched --> Adapters
  Adapters -->|polite, UA, rate-limited, robots-aware| EXT
```

**Principle:** the frontend never calls external government sites directly. All content flows
through the backend content API, which owns adapters, caching, and provenance.

## 2. Component responsibilities

- **Frontend (Next.js):** rendering, routing, SEO metadata, PDF reader UI, search UI, responsive
  + accessible components. Talks only to our API.
- **Controllers:** thin HTTP layer, validation, consistent error envelope, OpenAPI annotations.
- **Services:**
  - `ContentService` — classes/subjects/books/chapters/syllabus read model.
  - `SearchService` — PostgreSQL full-text search (pluggable to OpenSearch).
  - `PdfService` — resolves a chapter/book PDF to a redirect (deep link) or an unmodified proxy
    stream, with attribution headers; handles errors/retry semantics.
  - `SyncService` + `SourceAdapter`s — discovery, metadata extraction, normalization, checksum
    compare, versioning, change log, cache invalidation, re-index. Disabled by default.
  - `AiContentService` — grounded, clearly-labelled learning aids (pluggable provider; off by
    default in v1).
  - `TextToSpeechProvider` — pluggable interface, no hard external dependency in v1.
- **Repositories/JPA → PostgreSQL:** normalized schema (see data-model).
- **Redis:** caches hot read models (classes, subjects, books, chapters, syllabus) with TTLs.

## 3. Source adapter layer

```mermaid
flowchart LR
  Sync["SyncService"] --> IF{{"SourceAdapter interface"}}
  IF --> A1["NcertSourceAdapter"]
  IF --> A2["BepELotsSourceAdapter"]
  IF --> A3["ScertBiharSourceAdapter"]
  IF --> A4["DikshaSourceAdapter (future)"]
  A1 & A2 & A3 & A4 --> NM["Normalizer → internal model"]
  NM --> VAL["Validation + checksum"]
  VAL --> CL["ContentChangeLog (PENDING_REVIEW)"]
```

Each adapter normalizes its source's shape into the common `SourceContentDescriptor` model, so
adding a source does not change downstream code.

## 4. Content sync flow

```mermaid
sequenceDiagram
  participant Adm as Admin
  participant API as Sync API
  participant Sync as SyncService
  participant Ad as SourceAdapter
  participant Ext as Official Source
  participant DB as PostgreSQL
  participant Ch as Redis
  Adm->>API: POST /admin/sources/{id}/sync (RBAC)
  API->>Sync: run(sourceId)
  Sync->>Ad: discover()
  Ad->>Ext: fetch listing (polite, UA, backoff)
  Ext-->>Ad: HTML / PDF metadata
  Ad-->>Sync: descriptors + checksums
  Sync->>DB: compare versions, write ContentChangeLog(PENDING_REVIEW)
  Note over Sync,DB: never auto-overwrite OFFICIAL; new version pending
  Adm->>API: approve change
  API->>DB: promote to OFFICIAL, bump contentVersion
  API->>Ch: invalidate cache
  API->>Sync: reindex search
```

## 5. PDF flow

```mermaid
flowchart TD
  U["Student clicks Read/Download"] --> R["GET /api/v1/content/{id}/pdf"]
  R --> P["PdfService"]
  P --> D{"License + source policy"}
  D -->|link-only| RD["302 redirect to officialSourceUrl"]
  D -->|proxy allowed| ST["Stream unmodified bytes + attribution headers"]
  P --> E{"Source reachable?"}
  E -->|no| ERR["Return structured error → UI retry state"]
```

## 6. Search flow

```mermaid
flowchart LR
  Q["GET /api/v1/search?q="] --> S["SearchService"]
  S --> TS["Postgres to_tsvector / websearch_to_tsquery (simple + unaccent)"]
  TS --> RES["Ranked results: books, chapters, questions, summaries"]
```

## 7. AI content flow

```mermaid
flowchart TD
  C["Chapter source text"] --> G["AiContentService.generate(grounded)"]
  G --> L["Store with label=AI_GENERATED, model, promptVersion, sourceContentId"]
  L --> Rev["reviewStatus=PENDING_REVIEW"]
  Rev --> Adm["Admin approves → VERIFIED aid (never OFFICIAL)"]
```

## 8. Admin flow

```mermaid
flowchart LR
  Login["Admin login (JWT, ROLE_ADMIN)"] --> Dash["Admin dashboard"]
  Dash --> Src["Sources + Sync Now"]
  Dash --> Rev["Review queue (PENDING_REVIEW)"]
  Rev --> Act["Approve / Reject / Mark outdated"]
  Dash --> AI["AI content review"]
```

## 9. Technology & deployment

- Frontend: Next.js 14 (App Router), TypeScript, Tailwind, `pdf.js` based reader.
- Backend: Java 21, Spring Boot 3 (Web, Data JPA, Security, Validation, Actuator), springdoc
  OpenAPI, Flyway.
- Data: PostgreSQL 16 (+ `unaccent`), Redis 7.
- Containerized with Docker; `docker-compose.yml` for local; Kubernetes-compatible for prod.

```mermaid
flowchart LR
  subgraph compose["docker-compose (local dev)"]
    feC["frontend:3000"]
    beC["backend:8080"]
    pgC["postgres:5432"]
    rdC["redis:6379"]
  end
  feC --> beC --> pgC
  beC --> rdC
```

## 10. Extensibility (VI–XII)

Adding a class/subject/source is **data + config**: insert `classes`/`subjects` rows and (if
needed) register a new `SourceAdapter` bean. No page redesign — frontend reads the class/subject
catalogue from the API and renders generically.
