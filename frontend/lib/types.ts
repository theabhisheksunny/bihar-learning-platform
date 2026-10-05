// Shared types mirroring the backend DTOs (docs/api.md). Unverified fields may be null.

export interface Provenance {
  sourceType: string | null;
  sourceName: string | null;
  sourceUrl: string | null;
  sourceDocumentUrl: string | null;
  retrievedAt: string | null;
  publishedAt: string | null;
  academicYear: string | null;
  version: number | null;
  license: string | null;
  verificationStatus: string | null;
}

export interface ClassDto {
  id: string;
  code: string;
  name: string;
  active: boolean;
  displayOrder: number;
}

export interface SubjectDto {
  id: string;
  code: string;
  name: string;
  nameHi: string | null;
  icon: string | null;
  language: string;
  medium: string | null;
  academicYear: string | null;
  bookCount: number;
  chapterCount: number;
}

export interface BookSummaryDto {
  id: string;
  slug: string;
  title: string;
  subtitle: string | null;
  subject: string | null;
  language: string;
  academicYear: string | null;
  pdfAvailable: boolean;
  coverImage: string | null;
  chapterCount: number;
  verificationStatus: string;
}

export interface ChapterSummaryDto {
  id: string;
  chapterNumber: number;
  slug: string;
  title: string;
  titleHi: string | null;
  verificationStatus: string;
}

export interface BookDetailDto {
  id: string;
  slug: string;
  title: string;
  subtitle: string | null;
  board: string | null;
  publisher: string | null;
  language: string;
  medium: string | null;
  edition: string | null;
  isbn: string | null;
  coverImage: string | null;
  academicYear: string | null;
  pdfAvailable: boolean;
  downloadAllowed: boolean;
  officialSourceUrl: string | null;
  description: string | null;
  chapterCount: number;
  provenance: Provenance;
  chapters: ChapterSummaryDto[];
}

export interface ChapterDetailDto {
  id: string;
  chapterNumber: number;
  slug: string;
  title: string;
  titleHi: string | null;
  description: string | null;
  bookId: string;
  pdfAvailable: boolean;
  provenance: Provenance;
  availableSections: string[];
}

export interface ContentDto {
  id: string;
  contentType: string;
  body: string;
  language: string;
  ai: boolean;
  aiModel: string | null;
  aiReviewStatus: string | null;
  provenance: Provenance;
}

export interface QuestionDto {
  id: string;
  category: string;
  text: string;
  marks: number | null;
  difficulty: string | null;
  ai: boolean;
  verificationStatus: string;
  answers: string[];
}

export interface McqDto {
  id: string;
  question: string;
  options: string[];
  correctIndex: number;
  explanation: string | null;
  ai: boolean;
  verificationStatus: string;
}

export interface SyllabusDto {
  id: string;
  title: string;
  body: string | null;
  documentUrl: string | null;
  sourceName: string | null;
  sourceUrl: string | null;
  academicYear: string | null;
  verificationStatus: string;
}

export interface SearchResultDto {
  type: string;
  id: string;
  title: string;
  snippet: string;
  rank: number;
}

export interface PdfResolutionDto {
  mode: string; // STREAM (hosted) | LINK (external)
  url: string;
  sourceName: string | null;
  license: string | null;
  verificationStatus: string | null;
  note: string | null;
  accessMode: string | null;      // INTERNAL | EXTERNAL | EMBEDDED | DOWNLOAD | STREAM
  storageProvider: string | null;
  version: number | null;
  fileSize: number | null;
}

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  meta?: Record<string, unknown>;
  error?: { code: string; message: string };
}
