import Link from "next/link";
import type { BookSummaryDto } from "@/lib/types";
import { SourceBadge } from "./SourceBadge";

export function BookCard({ book }: { book: BookSummaryDto }) {
  return (
    <Link href={`/book/${book.slug}`} className="card group flex gap-4 p-4 focus-visible:outline-brand-600">
      <div
        aria-hidden
        className="grid h-28 w-20 shrink-0 place-items-center rounded-lg bg-gradient-to-br from-brand-100 to-brand-50 text-3xl"
      >
        📘
      </div>
      <div className="min-w-0">
        <p className="truncate text-lg font-semibold text-slate-900 group-hover:text-brand-700">{book.title}</p>
        {book.subtitle && <p className="text-sm text-slate-500">{book.subtitle}</p>}
        <p className="mt-1 text-sm text-slate-600">{book.chapterCount} chapters</p>
        <div className="mt-2">
          <SourceBadge
            provenance={{
              verificationStatus: book.verificationStatus,
              sourceName: null, sourceUrl: null, sourceType: null, sourceDocumentUrl: null,
              retrievedAt: null, publishedAt: null, academicYear: book.academicYear,
              version: null, license: null,
            }}
          />
        </div>
      </div>
    </Link>
  );
}
