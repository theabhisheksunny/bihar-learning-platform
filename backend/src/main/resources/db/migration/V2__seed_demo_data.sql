-- Seed data for the Bihar Class IX Student Learning Platform.
--
-- POLICY (see docs/content-policy.md & docs/source-research.md):
--   * NO fabricated official content. Unverified fields are NULL.
--   * Books/chapters below are labelled source_type='DEMO' and
--     verification_status='PENDING_REVIEW'. They carry REAL, OBSERVED source URLs only
--     (confirmed during Phase 1 research) and NULL everywhere a value was not verified.
--   * An admin must confirm each source's robots.txt/terms and APPROVE content before it
--     becomes OFFICIAL. Live crawling ships DISABLED.

-- ---------------------------------------------------------------------------
-- Classes (config-driven; only Class IX active for MVP)
-- ---------------------------------------------------------------------------
INSERT INTO classes (code, name, display_order, is_active) VALUES
  ('6',  'Class VI',   1, FALSE),
  ('7',  'Class VII',  2, FALSE),
  ('8',  'Class VIII', 3, FALSE),
  ('9',  'Class IX',   4, TRUE),
  ('10', 'Class X',    5, FALSE),
  ('11', 'Class XI',   6, FALSE),
  ('12', 'Class XII',  7, FALSE);

-- ---------------------------------------------------------------------------
-- Content sources (REAL base URLs; all DISABLED until an operator confirms terms)
-- ---------------------------------------------------------------------------
INSERT INTO content_sources (source_key, name, authority_tier, base_url, adapter_bean, enabled, robots_confirmed, license_default) VALUES
  ('NCERT',     'NCERT',               1, 'https://ncert.nic.in',            'ncertSourceAdapter',     FALSE, FALSE, 'CC BY-NC-ND 4.0'),
  ('BEP_ELOTS', 'BEP e-LOTS (Bihar)',  1, 'https://bepclots.bihar.gov.in',   'bepELotsSourceAdapter',  FALSE, FALSE, NULL),
  ('SCERT',     'SCERT Bihar',         1, 'https://scert.bihar.gov.in',      'scertBiharSourceAdapter',FALSE, FALSE, NULL),
  ('DIKSHA',    'DIKSHA',              1, 'https://diksha.gov.in',           NULL,                     FALSE, FALSE, 'CC BY-NC-ND / CC BY-NC-SA'),
  ('THIRD_PARTY','Third-party mirror', 3, NULL,                              NULL,                     FALSE, FALSE, NULL);

-- ---------------------------------------------------------------------------
-- Class IX subjects (NCERT-aligned; marked for review, not asserted as final list)
-- ---------------------------------------------------------------------------
INSERT INTO subjects (class_id, code, name, name_hi, icon, language, academic_year, display_order)
SELECT c.id, s.code, s.name, s.name_hi, s.icon, 'en', NULL, s.display_order
FROM classes c
CROSS JOIN (VALUES
  ('mathematics',    'Mathematics',    'गणित',        'calculator', 1),
  ('science',        'Science',        'विज्ञान',     'flask',      2),
  ('social-science', 'Social Science', 'सामाजिक विज्ञान','globe',   3),
  ('english',        'English',        'अंग्रेज़ी',   'book-open',   4),
  ('hindi',          'Hindi',          'हिन्दी',      'languages',   5),
  ('sanskrit',       'Sanskrit',       'संस्कृत',     'scroll',      6)
) AS s(code, name, name_hi, icon, display_order)
WHERE c.code = '9';

-- ---------------------------------------------------------------------------
-- DEMO books (REAL observed source URLs only; unverified metadata is NULL)
-- ---------------------------------------------------------------------------

-- Class IX Mathematics (NCERT code iemh1 confirmed; BEP e-LOTS /class-ix-maths/ confirmed)
INSERT INTO books (subject_id, source_id, slug, title, subtitle, board, publisher, language,
                   cover_image, pdf_url, official_source_url, description, chapter_count,
                   download_allowed, status,
                   source_type, source_name, source_url, source_document_url,
                   academic_year, license, verification_status)
SELECT subj.id, src.id,
       'bihar-class-9-mathematics',
       'Mathematics', 'Class IX', 'NCERT / Bihar', 'NCERT', 'en',
       NULL, NULL,
       'https://bepclots.bihar.gov.in/class-ix-maths/',
       'Class IX Mathematics. Official source links shown; metadata pending operator verification.',
       0, FALSE, 'DRAFT',
       'DEMO', 'BEP e-LOTS (Bihar) / NCERT',
       'https://bepclots.bihar.gov.in/class-ix-maths/',
       NULL,
       NULL, NULL, 'PENDING_REVIEW'
FROM subjects subj
JOIN classes cl ON cl.id = subj.class_id AND cl.code = '9'
JOIN content_sources src ON src.source_key = 'BEP_ELOTS'
WHERE subj.code = 'mathematics';

-- Class IX Social Science (SCERT Bihar "World of History" Class 9 PDF confirmed)
INSERT INTO books (subject_id, source_id, slug, title, subtitle, board, publisher, language,
                   cover_image, pdf_url, official_source_url, description, chapter_count,
                   download_allowed, status,
                   source_type, source_name, source_url, source_document_url,
                   academic_year, license, verification_status)
SELECT subj.id, src.id,
       'bihar-class-9-social-science-history',
       'Social Science: World of History', 'Class IX', 'SCERT Bihar', 'SCERT Bihar', 'en',
       NULL,
       'https://scert.bihar.gov.in/public/uploads/eresources/CLASS_9_WORLD_OF_HISTORY.pdf',
       'https://scert.bihar.gov.in',
       'Class IX Social Science (History) e-resource developed by SCERT Bihar. Pending operator verification.',
       0, FALSE, 'DRAFT',
       'DEMO', 'SCERT Bihar',
       'https://scert.bihar.gov.in',
       'https://scert.bihar.gov.in/public/uploads/eresources/CLASS_9_WORLD_OF_HISTORY.pdf',
       NULL, NULL, 'PENDING_REVIEW'
FROM subjects subj
JOIN classes cl ON cl.id = subj.class_id AND cl.code = '9'
JOIN content_sources src ON src.source_key = 'SCERT'
WHERE subj.code = 'social-science';

-- ---------------------------------------------------------------------------
-- DEMO chapters for Mathematics. Chapter TITLES mirror the public NCERT Class 9
-- Maths table of contents; each is DEMO/PENDING_REVIEW. No fabricated PDF URLs:
-- pdf_reference holds the known NCERT book code + chapter index as a hint for the
-- adapter to resolve, NOT a hardcoded live link.
-- ---------------------------------------------------------------------------
INSERT INTO chapters (book_id, chapter_number, slug, title, pdf_reference,
                      source_type, source_name, source_url, verification_status)
SELECT b.id, ch.n, ch.slug, ch.title, ch.ref,
       'DEMO', 'NCERT (code iemh1)', 'https://ncert.nic.in/textbook.php', 'PENDING_REVIEW'
FROM books b
CROSS JOIN (VALUES
  (1, 'number-systems',       'Number Systems',             'iemh101'),
  (2, 'polynomials',          'Polynomials',                'iemh102'),
  (3, 'coordinate-geometry',  'Coordinate Geometry',        'iemh103'),
  (4, 'linear-equations-two-variables', 'Linear Equations in Two Variables', 'iemh104'),
  (5, 'introduction-euclids-geometry',  'Introduction to Euclids Geometry',  'iemh105'),
  (6, 'lines-and-angles',     'Lines and Angles',           'iemh106'),
  (7, 'triangles',            'Triangles',                  'iemh107'),
  (8, 'quadrilaterals',       'Quadrilaterals',             'iemh108'),
  (9, 'circles',              'Circles',                    'iemh109'),
  (10,'herons-formula',       'Herons Formula',             'iemh110'),
  (11,'surface-areas-volumes','Surface Areas and Volumes',  'iemh111'),
  (12,'statistics',           'Statistics',                 'iemh112')
) AS ch(n, slug, title, ref)
WHERE b.slug = 'bihar-class-9-mathematics';

UPDATE books b SET chapter_count = (SELECT count(*) FROM chapters c WHERE c.book_id = b.id)
WHERE b.slug = 'bihar-class-9-mathematics';

-- ---------------------------------------------------------------------------
-- A single DEMO summary on chapter 1, clearly labelled, to exercise the UI path.
-- ---------------------------------------------------------------------------
INSERT INTO chapter_contents (chapter_id, content_type, body, language, is_ai,
                              source_type, source_name, verification_status)
SELECT ch.id, 'SUMMARY',
       'DEMO DATA (PENDING REVIEW): This placeholder shows where a chapter summary appears. '
       || 'Real summaries will be sourced from the official chapter or clearly labelled as an '
       || 'AI-generated learning aid grounded in the official text. It is NOT official content yet.',
       'en', FALSE, 'DEMO', 'Platform (demo)', 'PENDING_REVIEW'
FROM chapters ch
JOIN books b ON b.id = ch.book_id AND b.slug = 'bihar-class-9-mathematics'
WHERE ch.chapter_number = 1;

-- ---------------------------------------------------------------------------
-- Syllabus pointer (REAL BEP e-LOTS Class 9-10 curriculum page confirmed)
-- ---------------------------------------------------------------------------
INSERT INTO syllabus (class_id, subject_id, title, body, document_url,
                      source_type, source_name, source_url, verification_status)
SELECT cl.id, NULL,
       'Class 9-10 Curriculum (Bihar)',
       'Official Bihar Class 9-10 curriculum page. Open the source to view the authoritative document.',
       'https://bepclots.bihar.gov.in/syllabus-class-9-to-class-10/',
       'DEMO', 'BEP e-LOTS (Bihar)',
       'https://bepclots.bihar.gov.in/syllabus-class-9-to-class-10/',
       'PENDING_REVIEW'
FROM classes cl WHERE cl.code = '9';
