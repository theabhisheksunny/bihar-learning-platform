# Content Policy — Bihar Class IX Student Learning Platform

This policy governs how content enters, lives in, and is displayed by the platform. It exists to
keep the platform **legally compliant**, **honest about provenance**, and **safe for students**.

## 1. Licensing reality (from `source-research.md`)

- **NCERT textbooks are licensed CC BY-NC-ND 4.0** — Attribution, **Non-Commercial**,
  **No-Derivatives**.
- **Other DIKSHA resources are CC BY-NC-SA** — derivatives allowed only if shared alike and
  non-commercial.
- Bihar Govt portals (BEP e-LOTS, SCERT) host NCERT-aligned material; treated as
  CC BY-NC-ND-equivalent until a portal states otherwise.

### What CC BY-NC-ND means for us
| Allowed | Not allowed |
|---------|-------------|
| Store & display **metadata** (title, subject, chapters, source URL, year) | Charging money for access to the official content |
| **Link** to the official PDF | Publishing a **modified** version of the official text and calling it the textbook |
| **Stream/proxy the unmodified** PDF with clear attribution | Removing attribution / hiding the source |
| Create **separately-labelled** AI learning aids *about* the content | Presenting AI/derived text as the official NCERT/Bihar content |

> Because the project is a public-good educational platform, re-hosting an **unmodified** copy
> with attribution and no fee is the conservative interpretation of "non-commercial". The code
> **defaults to deep-linking / proxying**, and re-hosting a cached copy is gated behind an admin
> decision per source. If in doubt, link — don't mirror.

## 2. Provenance is mandatory

Every content record carries a provenance block (see `data-model.md`):
`sourceType, sourceName, sourceUrl, sourceDocumentUrl, retrievedAt, publishedAt, academicYear,
version, checksum, license, verificationStatus`.

**Verification statuses:** `OFFICIAL`, `VERIFIED`, `THIRD_PARTY`, `AI_GENERATED`,
`PENDING_REVIEW`, `OUTDATED`.

- Nothing is shown as `OFFICIAL` without passing the admin approval gate.
- The source is **always visible** to students (e.g. "Source: NCERT · Academic Year 2026–27").

## 3. No fake data (hard rule)

- Never fabricate official books, syllabi, government PDFs, chapters, APIs, source URLs, or exam
  questions.
- Unverified fields are stored as `null`, never guessed.
- Any sample/placeholder content is tagged `DEMO DATA` and `PENDING_REVIEW`, visibly, and uses a
  real source URL only when one was actually observed.

## 4. AI-generated content

- Always labelled **"AI-generated learning aid"** in the UI.
- Must be **grounded** in a specific `sourceContentId` (the chapter it summarises).
- Stores `generatedAt, model, sourceContentId, promptVersion, reviewStatus`.
- Never labelled `OFFICIAL`. Never claims to be the textbook or a "previous exam question" unless
  that provenance is independently `VERIFIED`.

## 5. Crawling / ingestion ethics

- Respect `robots.txt` and each site's terms (confirm before enabling an adapter).
- Polite rate limiting, realistic User-Agent, retry-with-backoff; **sync disabled by default**.
- **Never** bypass authentication, CAPTCHAs, access controls, paywalls, DRM, or technical
  restrictions. **Never** build an unauthorized scraper of protected areas.

## 6. Student safety

- No login required to read. Minimal data collection. No ads, no payments, no inter-student chat
  in the MVP.
- PII uses placeholders in examples; real PII only where a user voluntarily provides it for their
  own account features (bookmarks/progress), stored securely.
