import Link from "next/link";
import type { SubjectDto } from "@/lib/types";

const ICONS: Record<string, string> = {
  calculator: "🧮",
  flask: "🔬",
  globe: "🌍",
  "book-open": "📖",
  languages: "🔤",
  scroll: "📜",
};

export function SubjectCard({ classCode, subject }: { classCode: string; subject: SubjectDto }) {
  const emoji = subject.icon ? ICONS[subject.icon] ?? "📘" : "📘";
  return (
    <Link
      href={`/class/${classCode}/${subject.code}`}
      className="card group flex flex-col gap-3 p-5 focus-visible:outline-brand-600"
    >
      <div className="flex items-center gap-3">
        <span aria-hidden className="grid h-12 w-12 place-items-center rounded-xl bg-brand-50 text-2xl">
          {emoji}
        </span>
        <div>
          <p className="text-lg font-semibold text-slate-900 group-hover:text-brand-700">{subject.name}</p>
          {subject.nameHi && <p className="text-sm text-slate-500">{subject.nameHi}</p>}
        </div>
      </div>
      <dl className="mt-1 flex gap-4 text-sm text-slate-600">
        <div><dt className="sr-only">Books</dt><dd>{subject.bookCount} books</dd></div>
        <div><dt className="sr-only">Chapters</dt><dd>{subject.chapterCount} chapters</dd></div>
      </dl>
    </Link>
  );
}
