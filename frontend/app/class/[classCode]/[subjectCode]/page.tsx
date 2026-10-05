import type { Metadata } from "next";
import Link from "next/link";
import { api } from "@/lib/api";
import { BookCard } from "@/components/BookCard";
import { EmptyState } from "@/components/EmptyState";

export const revalidate = 60;

async function resolveSubject(classCode: string, subjectCode: string) {
  const res = await api.subjects(classCode);
  if (!res.success || !res.data) return undefined;
  return res.data.find((s) => s.code === subjectCode);
}

export async function generateMetadata(
  { params }: { params: { classCode: string; subjectCode: string } }
): Promise<Metadata> {
  const subject = await resolveSubject(params.classCode, params.subjectCode);
  const name = subject?.name ?? params.subjectCode;
  return {
    title: `${name} — Class ${params.classCode} books`,
    description: `Bihar Board Class ${params.classCode} ${name} books and chapters.`,
    alternates: { canonical: `/class/${params.classCode}/${params.subjectCode}` },
  };
}

export default async function SubjectPage(
  { params }: { params: { classCode: string; subjectCode: string } }
) {
  const subject = await resolveSubject(params.classCode, params.subjectCode);
  const booksRes = subject ? await api.books(subject.id) : undefined;
  const books = booksRes?.success && booksRes.data ? booksRes.data : [];

  return (
    <div className="container-x py-10">
      <nav aria-label="Breadcrumb" className="mb-3 text-sm text-slate-500">
        <Link href="/" className="hover:text-brand-600">Home</Link> <span aria-hidden>/</span>{" "}
        <Link href={`/class/${params.classCode}`} className="hover:text-brand-600">Class {params.classCode}</Link>{" "}
        <span aria-hidden>/</span> <span className="text-slate-700">{subject?.name ?? params.subjectCode}</span>
      </nav>
      <h1 className="text-3xl font-extrabold text-slate-900">{subject?.name ?? params.subjectCode}</h1>
      {subject?.nameHi && <p className="mt-1 text-slate-500">{subject.nameHi}</p>}

      <h2 className="mt-8 text-xl font-bold text-slate-900">Books</h2>
      <div className="mt-5 grid gap-4 sm:grid-cols-2">
        {books.length > 0 ? (
          books.map((b) => <BookCard key={b.id} book={b} />)
        ) : (
          <div className="sm:col-span-2">
            <EmptyState
              title="No books available yet"
              message="Books for this subject are pending verification or not yet added."
            />
          </div>
        )}
      </div>
    </div>
  );
}
