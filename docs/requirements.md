# Requirements — Bihar Class IX Student Learning Platform

**Status:** Phase 2 deliverable. Builds on `source-research.md` and `content-policy.md`.
**Scope of MVP:** Class IX, architected so Classes VI–XII are a content/config task later.

---

## 1. Product goal

A single trusted digital destination where a Bihar Class IX student does:

> **Select class → subject → book → chapter → read / summarise / practise.**

Modern educational product feel (not a government portal). Mobile-first, fast, accessible,
honest about sources.

## 2. Primary users

- **Student** (age ~13–15): discover, read, understand, practise. No login required to read.
- **Admin / content operator:** manage sources, trigger sync, review & approve content, manage
  AI-generated aids.
- (Future) Teacher, parent.

## 3. Functional requirements

### 3.1 Discovery & navigation
- **FR-1** Homepage with hero, quick-action tiles (Books, Chapters, Questions, Summaries,
  Syllabus), Class IX subject grid, "latest content updates", and about section.
- **FR-2** Class selection. MVP shows Class 9 active; Classes 6/7/8/10/11/12 present but marked
  "coming soon" and **config-driven** (adding one is data, not a redesign).
- **FR-3** Class IX dashboard: subject cards with icon, book count, chapter count, syllabus
  status, language/medium, academic year.
- **FR-4** Subject page → list of books for that subject.
- **FR-5** Book details page: cover, title, subject, class, board, academic year, publisher,
  **source + license badge**, PDF availability, Read button, Download button *(only when the
  license/source permits)*, chapter list, in-book search, progress.
- **FR-6** Chapter listing per book (ordered by chapter number).
- **FR-7** Chapter page with tabbed sections: Overview, Summary, Read Chapter, Important Points,
  Questions, Answers, MCQs, Exercise Solutions, Revision. Sections with no verified content show
  an honest empty/`PENDING_REVIEW` state, not fabricated text.
- **FR-8** Syllabus view per class/subject with source attribution.

### 3.2 Reading
- **FR-9** Browser PDF reader: zoom, in-PDF search, page navigation, prev/next, page number,
  fullscreen, keyboard shortcuts, loading state, **error state + retry**, PDF metadata, lazy
  loading. Must degrade gracefully (scanned PDFs, broken URLs).
- **FR-10** PDFs are served **via the backend content endpoint**, never by hardcoding external
  gov URLs in the frontend. Backend may redirect/deep-link or proxy the **unmodified** file with
  attribution.

### 3.3 Search
- **FR-11** Global search across books, chapters, questions, summaries, topics. Multilingual
  (English/Hindi/Urdu where content exists). PostgreSQL full-text search for MVP; architecture
  open to OpenSearch later.

### 3.4 Student aids
- **FR-12** Quick Summary per chapter (what it teaches, key points, definitions, formulas,
  common questions) — **labelled as AI-generated learning aid** when generated, grounded in the
  source chapter.
- **FR-13** Questions by category: MCQ, Very Short, Short, Long, Important, Previous Exam
  (the last only if source-`VERIFIED`).
- **FR-14** (Optional MVP) bookmarks & reading progress for a lightweight local/account profile.

### 3.5 Content provenance & admin
- **FR-15** Every content item exposes provenance (source, year, license, verification status);
  source is always visible to students.
- **FR-16** Admin panel: view sources/books/chapters/sync jobs; trigger sync; approve/reject/mark
  outdated; view content versions; view & approve AI content.
- **FR-17** Sync dashboard per source: status, last checked, last successful sync, records
  discovered/updated/failed, "Sync Now" for authorized admins.
- **FR-18** Content sync engine: scheduler + adapters; checksum/version/date comparison; change
  detection; versioning; change log; cache invalidation; search re-index; sync history. **Live
  crawling disabled by default**; admin approval gate before `OFFICIAL`.

### 3.6 Content integrity rules (from content-policy)
- **FR-19** No fabricated official content, APIs, URLs, or exam questions. Unverified fields are
  `null`; sample content tagged `DEMO DATA` + `PENDING_REVIEW`.
- **FR-20** Never present AI/derived text as official. Never represent third-party sources as
  government content.

## 4. Non-functional requirements

- **NFR-1 Performance:** LCP < 2.5s, CLS < 0.1, INP < 200ms where practical. Lazy loading, code
  splitting, image optimization, caching (Redis), API pagination, DB indexing, CDN-ready. No
  loading all books/chapters on the homepage.
- **NFR-2 Responsive:** intentional layouts at 360/390/768/1024/1280/1440px+. Mobile-first.
- **NFR-3 Accessibility:** semantic HTML, keyboard nav, ARIA where needed, readable fonts, good
  contrast, focus indicators, alt text, accessible PDF controls. (Full WCAG conformance requires
  manual AT testing + expert review — noted as a limitation.)
- **NFR-4 SEO:** per-page metadata, sitemap, robots.txt, canonical URLs, Open Graph, structured
  data for class/subject/book/chapter pages. Human-readable URLs.
- **NFR-5 Security:** input validation, rate limiting, CORS, secure headers, admin API auth +
  RBAC, secret management via env, SQLi protection (JPA/parameterized), XSS/CSRF protection,
  dependency scanning. No secrets in frontend.
- **NFR-6 Observability:** structured logging (API/sync/source/content/errors/PDF/search),
  Prometheus/Grafana/Loki-ready. No unnecessary PII in logs.
- **NFR-7 Internationalization:** architecture supports English/Hindi/Urdu; official vs.
  translated-aid always distinguishable.
- **NFR-8 Extensibility:** adding a class/subject/source is configuration + ingestion, not a
  rewrite. Source adapters are pluggable; TTS provider is a pluggable interface (no hard external
  TTS dependency in v1).
- **NFR-9 API-first & versioned:** `/api/v1/...`. Frontend never calls external gov sites
  directly. OpenAPI/Swagger in dev/admin.
- **NFR-10 Portability:** Docker + docker-compose for local; Kubernetes-compatible for prod.

## 5. Key user journeys

1. **Read a chapter:** Home → Explore Class 9 → Mathematics → book → Chapter 1 → Read Chapter
   (PDF) / Summary tab.
2. **Practise:** chapter → Questions tab → filter MCQ → reveal answers.
3. **Find by search:** type "number systems" → results across books/chapters/questions → open.
4. **Check syllabus:** Home → View Syllabus → Class 9 Mathematics → source-attributed syllabus.
5. **Admin sync:** Admin → Sources → BEP e-LOTS → Sync Now → review discovered items → approve →
   content goes live as `OFFICIAL`.

## 6. Assumptions

- Bihar Class IX largely adopts NCERT-aligned textbooks (supported by research; confirmed for
  Maths and SCERT Social-Science material).
- Content lives as static PDFs; no public API available at MVP time.
- Operators will confirm robots.txt/terms per source before enabling live crawl.

## 7. Limitations (honest)

- Exact live PDF URL templates and full subject catalogue are `UNVERIFIED` and must be confirmed
  by an operator; seed ships as `DEMO DATA`/`PENDING_REVIEW` with real URLs only where observed.
- DIKSHA API access model unconfirmed → adapter deferred.
- TTS and full multilingual translation are architected but not fully implemented in v1.
- Full WCAG/AT validation is out of automated scope.

## 8. Out of scope for MVP (do not overbuild)

Social networking, student chat, heavy gamification, payments, ads, mandatory auth, complex
recommendation engines.

## 9. Legal / content constraints (binding)

Per `content-policy.md`: CC BY-NC-ND for NCERT text (no derivatives presented as official, non
-commercial, attribution preserved); provenance always shown; no bypassing access controls; no
fake data; AI content clearly labelled and grounded.
