import type {
  ApiResponse, BookDetailDto, BookSummaryDto, ChapterDetailDto, ChapterSummaryDto,
  ClassDto, ContentDto, McqDto, PdfResolutionDto, QuestionDto, SearchResultDto,
  SubjectDto, SyllabusDto,
} from "./types";

/**
 * Single API client. The browser only ever talks to our own backend (proxied via /api), never
 * to external government sites directly. Server components call the backend directly.
 */
const BASE =
  typeof window === "undefined"
    ? (process.env.BACKEND_URL || "http://localhost:8080") + "/api/v1"
    : "/api/v1";

async function get<T>(path: string, revalidate = 60): Promise<ApiResponse<T>> {
  try {
    const res = await fetch(`${BASE}${path}`, {
      headers: { Accept: "application/json" },
      next: { revalidate },
    });
    if (!res.ok) {
      const body = (await res.json().catch(() => null)) as ApiResponse<T> | null;
      return body ?? { success: false, error: { code: "HTTP_" + res.status, message: res.statusText } };
    }
    return (await res.json()) as ApiResponse<T>;
  } catch (e) {
    return { success: false, error: { code: "NETWORK_ERROR", message: "Could not reach the server" } };
  }
}

export const api = {
  classes: () => get<ClassDto[]>("/classes"),
  classDetail: (id: string) => get<ClassDto>(`/classes/${id}`),
  subjects: (classId: string) => get<SubjectDto[]>(`/classes/${classId}/subjects`),
  subject: (id: string) => get<SubjectDto>(`/subjects/${id}`),
  books: (subjectId: string) => get<BookSummaryDto[]>(`/subjects/${subjectId}/books`),
  book: (id: string) => get<BookDetailDto>(`/books/${id}`),
  chapters: (bookId: string) => get<ChapterSummaryDto[]>(`/books/${bookId}/chapters`),
  chapter: (id: string) => get<ChapterDetailDto>(`/chapters/${id}`),
  chapterContent: (id: string, type?: string) =>
    get<ContentDto[]>(`/chapters/${id}/content${type ? `?type=${type}` : ""}`),
  questions: (id: string, category?: string) =>
    get<QuestionDto[]>(`/chapters/${id}/questions${category ? `?category=${category}` : ""}`),
  mcqs: (id: string) => get<McqDto[]>(`/chapters/${id}/mcqs`),
  syllabus: (classId: string, subjectId?: string) =>
    get<SyllabusDto[]>(`/syllabus/${classId}${subjectId ? `?subjectId=${subjectId}` : ""}`),
  search: (q: string, type?: string) =>
    get<SearchResultDto[]>(`/search?q=${encodeURIComponent(q)}${type ? `&type=${type}` : ""}`, 0),
  bookPdf: (bookId: string) => get<PdfResolutionDto>(`/content/book/${bookId}/pdf`, 0),
  chapterPdf: (chapterId: string) => get<PdfResolutionDto>(`/content/chapter/${chapterId}/pdf`, 0),
};
