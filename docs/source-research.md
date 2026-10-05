# Source Research — Bihar Class IX Student Learning Platform

**Status:** Phase 1 deliverable (research before implementation).
**Last updated:** 2026-10-04
**Research method:** Web search + attempted direct fetches. Where a fact could not be
verified directly it is explicitly marked `UNVERIFIED`. **No APIs, URLs, or metadata have
been invented.**

> ⚠️ **Important caveat on verification depth.** Several government sites
> (`bepclots.bihar.gov.in`) actively reset non-browser connections (`ECONNRESET`) to the
> automated fetcher used during this research. Page *existence* and URL *patterns* were
> confirmed through the search index, but full HTML/DOM structure and `robots.txt` contents
> could **not** be fetched programmatically from this environment. The ingestion layer is
> therefore designed defensively: resilient adapters, retries, polite rate limiting, caching,
> and a manual/admin review gate. Any field below marked `UNVERIFIED` must be confirmed by a
> human operator (ideally from a normal browser) before relying on it in production.

---

## Summary of findings

| # | Source | Authority tier | Public API? | PDFs? | License | Recommended integration |
|---|--------|----------------|-------------|-------|---------|--------------------------|
| 1 | NCERT (`ncert.nic.in`, `epathshala.nic.in`) | Tier 1 (national) | **No public documented API** | Yes (per-chapter + full book PDFs) | **CC BY-NC-ND** (textbooks) | Store metadata + deep links; **link/stream, do not re-host modified copies** |
| 2 | BEP e-LOTS (`bepclots.bihar.gov.in`) | Tier 1 (Bihar Govt / Education Dept) | **No public documented API** | Yes (per-chapter PDFs, WordPress uploads) | Not explicitly stated; govt portal hosting NCERT-aligned content → treat as **NCERT-equivalent (CC BY-NC-ND)** until confirmed | Metadata + deep links; cache with admin approval |
| 3 | SCERT Bihar (`scert.bihar.gov.in`) | Tier 1 (Bihar Govt) | **No public documented API** | Yes (`/public/uploads/eresources/*.pdf`) | Not explicitly stated → `UNVERIFIED`, treat conservatively | Metadata + deep links |
| 4 | DIKSHA (`diksha.gov.in`) | Tier 1 (national, NEP 2020) | **Platform is open-source (Sunbird); content APIs exist but require registration/keys** `UNVERIFIED` for public anon use | Yes (Energized Textbooks) | **NCERT textbooks: CC BY-NC-ND; other DIKSHA resources: CC BY-NC-SA** | Future adapter; requires channel/API-key research |
| 5 | Bihar Board Books (`biharboardbooks.com`) | **Tier 2/3 (third-party, NOT government)** | No | Yes (mirrors) | Third-party mirror; copyright unclear → `UNVERIFIED` | **Do not treat as official.** Supplementary only, clearly labelled |

**Headline conclusion:** There is **no public, documented, authentication-free REST API** for
Bihar/NCERT Class IX textbook content discoverable in this research. Content is published as
**static PDF files** on government web portals. The correct, compliant architecture is:

1. Store **metadata** (titles, subjects, chapters, source URLs, academic year) in our DB.
2. **Deep-link** to the official PDF, or stream/proxy it through our backend **without
   modification**, with the official source clearly attributed on every page.
3. Treat AI summaries/questions as clearly-labelled *learning aids*, never as official content
   (CC BY-NC-**ND** forbids presenting derivatives of NCERT text as the textbook).
4. Keep a human **approval gate** before any content is marked `OFFICIAL`.

---

## 1. NCERT — `ncert.nic.in` / `epathshala.nic.in`

- **Authority:** National Council of Educational Research and Training. Tier 1. Bihar Board
  Class IX largely adopts NCERT textbooks (confirmed indirectly: BEP e-LOTS and SCERT Bihar
  host NCERT-aligned Maths/Science/etc.).
- **API:** No public, documented REST API found. The official catalogue is a server-rendered
  page at `https://ncert.nic.in/textbook.php`.
- **PDF availability:** Yes. NCERT exposes per-book codes and per-chapter PDFs. Observed book
  code pattern (verified via archive.org mirror of the official item): Class 9 Maths = `iemh1`,
  with chapters `iemh101`, `iemh102`, … `iemh1an` (answers/appendix). The historical direct
  pattern is of the form `https://ncert.nic.in/textbook/pdf/<code><chapter>.pdf`.
  > `UNVERIFIED`: exact current live URL template must be reconfirmed from a browser; NCERT has
  > changed its download handler over time. **Do not hardcode — resolve via the adapter + admin
  > confirmation.**
- **Metadata availability:** Class, subject, book title, chapter list derivable from the
  catalogue page. ISBN/edition sometimes present on the book's inner cover PDF.
- **Syllabus:** NCERT publishes syllabus documents separately (PDF).
- **Terms / License:** **CC BY-NC-ND 4.0** for NCERT textbooks (source: Digital India
  Corporation / DIKSHA statement: "NCERT Textbooks are licenced under CC BY NC-ND"). This is
  **Non-Commercial, No-Derivatives**. Implications are documented in `content-policy.md`.
- **robots.txt:** `UNVERIFIED` (could not fetch). Must be checked before any automated crawl.
- **Update frequency:** Annually around academic session change (e.g. 2026–27 editions
  referenced in current search results).
- **Recommended integration:** `NcertSourceAdapter` — resolve book/chapter metadata, store deep
  links, optionally stream the unmodified PDF through the backend proxy with attribution.
  Content was rephrased for compliance with licensing restrictions.

## 2. BEP e-LOTS — `bepclots.bihar.gov.in`

- **Authority:** e-LIBRARY OF TEACHERS AND STUDENTS, developed by the **Education Department,
  Government of Bihar** (Bihar Education Project Council). Tier 1 for Bihar. (Confirmed: the
  homepage is a Bihar Govt message about digital education for classes 1–12.)
- **API:** No public API. It is a **WordPress** site (upload paths like
  `/wp-content/uploads/2021/05/...pdf` and a `/category/.../syllabus/` taxonomy observed).
- **PDF availability:** Yes. Per-subject pages exist, e.g. **`/class-ix-maths/`** (Class IX
  Maths — गणित, confirmed to exist). Chapter PDFs confirmed, e.g.
  `/ptyrdimu/2021/05/Class-10GanitGanitChapter-7.pdf` (pattern shows class + subject + chapter).
  A Class 9–10 new-curriculum/syllabus page exists at `/syllabus-class-9-to-class-10/`.
- **Metadata availability:** Class → subject → chapter structure is derivable from the page
  taxonomy. Subject names appear bilingually (English + Hindi, e.g. "Maths (गणित)").
- **Syllabus:** Yes — dedicated syllabus archive and the Class 9–10 curriculum page.
- **Terms / License:** Not explicitly stated in search results → `UNVERIFIED`. Content is
  NCERT-aligned Bihar textbook material; treat as **CC BY-NC-ND-equivalent** (conservative)
  until a human confirms the portal's own terms.
- **robots.txt:** `UNVERIFIED` — the site reset automated connections. Must be checked from a
  browser before crawling.
- **Access restrictions:** The site **actively drops non-browser connections** (observed
  `ECONNRESET`). Any adapter must send a realistic User-Agent, respect rate limits, retry with
  backoff, and ideally run behind a human-triggered admin sync rather than aggressive crawling.
- **Update frequency:** Low (uploads dated 2021, taxonomy updated 2024). Periodic sync (weekly)
  is sufficient.
- **Recommended integration:** `BepELotsSourceAdapter` (a.k.a. `BepcSourceAdapter`) — scrape the
  known subject pages politely, extract chapter titles + PDF links, store metadata + deep links.
  **Admin approval required** before publishing. **No bypassing of any access control.**

## 3. SCERT Bihar — `scert.bihar.gov.in`

- **Authority:** State Council of Educational Research & Training, Bihar. Tier 1 for Bihar.
- **API:** No public API found.
- **PDF availability:** Yes — e-resources path confirmed, e.g.
  `/public/uploads/eresources/CLASS_9_WORLD_OF_HISTORY.pdf` ("Developed by State Council of
  Educational Research & Training, Bihar"). Indicates SCERT-authored Class 9 material.
- **Metadata / Syllabus:** Partially derivable from filenames and listing pages → `UNVERIFIED`
  for a complete catalogue.
- **License:** Not stated → `UNVERIFIED`, treat conservatively (metadata + link).
- **robots.txt:** `UNVERIFIED`.
- **Recommended integration:** `ScertBiharSourceAdapter` (future / Tier 1). Metadata + deep
  links first.

## 4. DIKSHA — `diksha.gov.in`

- **Authority:** National platform under NEP 2020 (Digital Infrastructure for Knowledge
  Sharing). Open-source (Sunbird). Tier 1.
- **API:** DIKSHA/Sunbird exposes content search/read APIs, but public anonymous access,
  channel scoping, and API-key requirements are **`UNVERIFIED`** here and need dedicated
  research + registration before use. The platform concept of "Energized Textbooks" maps QR
  codes to digital content.
- **License (confirmed):** **NCERT textbooks: CC BY-NC-ND.** **Other DIKSHA resources:
  CC BY-NC-SA.** (Source: Digital India Corporation.) The SA (ShareAlike) resources are more
  permissive for derivatives *if* we share alike and stay non-commercial.
- **Recommended integration:** Defer to a later `DikshaSourceAdapter`. First confirm API access
  model and per-asset license programmatically (each asset carries its own license field).

## 5. Bihar Board Books — `biharboardbooks.com`

- **Authority:** **Third-party website, NOT a government source.** Tier 2/3.
- **API:** None.
- **PDFs:** Mirrors NCERT/Bihar PDFs.
- **License / copyright:** Unclear / `UNVERIFIED`. Because it is a non-government mirror of
  copyrighted NCERT material, **it must never be represented as official**.
- **Recommended integration:** Lowest priority. If used at all, label strictly as
  Tier 3 supplementary with a visible "unofficial source" badge. Prefer the Tier 1 government
  original every time it is available.

---

## robots.txt / crawl-policy status (action required)

| Domain | robots.txt fetched? | Decision |
|--------|---------------------|----------|
| `ncert.nic.in` | No (`UNVERIFIED`) | Confirm before automated crawl; prefer metadata + deep link |
| `bepclots.bihar.gov.in` | No — connection reset (`UNVERIFIED`) | Confirm before crawl; polite, rate-limited, admin-triggered sync only |
| `scert.bihar.gov.in` | No (`UNVERIFIED`) | Confirm before crawl |
| `diksha.gov.in` | No (`UNVERIFIED`) | Use official API path once access model is confirmed |

**Rule enforced in code:** the sync engine will ship **disabled by default** for live crawling.
An operator must (a) confirm each source's robots.txt + terms, (b) enable the adapter, and
(c) approve discovered content before it becomes publicly visible as `OFFICIAL`.

---

## Confirmed vs. assumed (honesty ledger)

**Confirmed by search index:**
- BEP e-LOTS is a Bihar Govt portal for classes 1–12; `/class-ix-maths/` exists; chapter PDFs
  exist under WordPress upload paths; a Class 9–10 syllabus page exists.
- SCERT Bihar hosts Class 9 e-resources PDFs.
- NCERT Class 9 Maths book code `iemh1` and per-chapter files exist.
- NCERT textbooks are CC BY-NC-ND; other DIKSHA resources are CC BY-NC-SA.

**NOT confirmed (`UNVERIFIED`) — require human/browser confirmation before production:**
- Exact live PDF URL templates (handlers change).
- Full subject list for Bihar Class IX (see note below).
- robots.txt contents for every domain.
- DIKSHA public API access model and keys.
- Each portal's explicit terms-of-use text.

**Class IX subject list — current stance:** Do **not** hardcode a final list. Research confirms
at least **Maths** and (via SCERT) **History/Social Science** material for Class 9 under Bihar
portals, and NCERT Class 9 covers Maths, Science, Social Science, English, Hindi, Sanskrit. The
seed data will carry these as **`DEMO DATA` / `PENDING_REVIEW`** with real source URLs where a
URL was observed, and `null` for every unverified field — never fabricated. The admin confirms
and promotes to `OFFICIAL`.

---

## Recommended integration method (overall)

```
Scheduler / Admin "Sync Now"
      ↓
Source Adapter (polite, UA + rate-limit + retry/backoff, robots-aware)
      ↓
Discovery (list subject/chapter pages)  →  Metadata extraction
      ↓
Validation (URL reachable? class/subject/chapter present?)  →  Normalization to internal model
      ↓
Checksum/version compare  →  ContentChangeLog  →  PENDING_REVIEW
      ↓
Admin approval  →  OFFICIAL  →  cache + search index
```

No scraping of authenticated areas, no CAPTCHA solving, no DRM circumvention, no re-hosting of
modified official content. See `content-policy.md`.
