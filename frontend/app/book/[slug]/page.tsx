import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { api } from "@/lib/api";
import { SourceBadge } from "@/components/SourceBadge";

export const revalidate = 60;

export async function generateMetadata({ params }: { params: { slug: string } }): Promise<Metadata> {
  const res = await api.book(params.slug);
  const book = res.success ? res.data : undefined;
  if (!book) return { title: "Book" };
  return {
    title: `${book.title}${book.subtitle ? " — " + book.subtitle : ""}`,
    description: book.description ?? `Chapters and study material for ${book.title}.`,
    alternates: { canonical: `/book/${book.slug}` },
  };
}

export default async function BookPage({ params }: { params: { slug: string } }) {
  const res = await api.book(params.slug);
  if (!res.success || !res.data) notFound();
  const book = res.data;

  return (
    <div className="container-x py-10">
      <nav aria-label="Breadcrumb" className="mb-3 text-sm text-slate-500">
        <Link href="/" className="hover:text-brand-600">Home</Link> <span aria-hidden>/</span>{" "}
        <span className="text-slate-700">{book.title}</span>
      </nav>

      <div className="grid gap-8 lg:grid-cols-[260px_1fr]">
        <aside>
          <div
            aria-hidden
            className="grid aspect-[3/4] w-full place-items-center rounded-2xl bg-gradient-to-br from-brand-100 to-brand-50 text-6xl shadow-sm"
          >
            📘
          </div>
          <dl className="mt-4 space-y-1 text-sm text-slate-600">
            {book.publisher && <div><dt className="inline font-semibold">Publisher: </dt><dd className="inline">{book.publisher}</dd></div>}
            {book.board && <div><dt className="inline font-semibold">Board: </dt><dd className="inline">{book.board}</dd></div>}
            <div><dt className="inline font-semibold">Language: </dt><dd className="inline">{book.language}</dd></div>
            <div><dt className="inline font-semibold">Chapters: </dt><dd className="inline">{book.chapterCount}</dd></div>
          </dl>
        </aside>

        <div>
          <h1 className="text-3xl font-extrabold text-slate-900">{book.title}</h1>
          {book.subtitle && <p className="mt-1 text-lg text-slate-500">{book.subtitle}</p>}
          {book.description && <p className="mt-4 max-w-2xl text-slate-600">{book.description}</p>}

          <div className="mt-4"><SourceBadge provenance={book.provenance} /></div>

          <div className="mt-6 flex flex-wrap gap-3">
            {book.pdfAvailable ? (
              <a
                href={book.officialSourceUrl ?? book.provenance.sourceDocumentUrl ?? "#"}
                target="_blank"
                rel="noopener noreferrer"
                className="btn-primary"
              >
                Read / open official source
              </a>
            ) : (
              <span className="rounded-xl bg-slate-100 px-5 py-3 text-sm text-slate-500">
                PDF not available yet
              </span>
            )}
            <Link href={`/syllabus/9`} className="btn-secondary">View Syllabus</Link>
          </div>

          <h2 className="mt-10 text-xl font-bold text-slate-900">Chapters</h2>
          {book.chapters.length > 0 ? (
            <ol className="mt-4 divide-y divide-slate-100 overflow-hidden rounded-2xl border border-slate-200 bg-white">
              {book.chapters.map((ch) => (
                <li key={ch.id}>
                  <Link
                    href={`/book/${book.slug}/chapter/${ch.id}`}
                    className="flex items-center justify-between gap-4 px-5 py-4 hover:bg-slate-50"
                  >
                    <span className="flex items-center gap-4">
                      <span className="grid h-9 w-9 shrink-0 place-items-center rounded-lg bg-brand-50 text-sm font-bold text-brand-700">
                        {String(ch.chapterNumber).padStart(2, "0")}
                      </span>
                      <span className="font-medium text-slate-800">{ch.title}</span>
                    </span>
                    <span aria-hidden className="text-slate-400">→</span>
                  </Link>
                </li>
              ))}
            </ol>
          ) : (
            <p className="mt-4 text-slate-600">No chapters added yet.</p>
          )}
        </div>
      </div>
    </div>
  );
}
