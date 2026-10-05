import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { api } from "@/lib/api";
import { ChapterTabs } from "@/features/chapter/ChapterTabs";

export const revalidate = 60;

export async function generateMetadata(
  { params }: { params: { slug: string; chapterId: string } }
): Promise<Metadata> {
  const res = await api.chapter(params.chapterId);
  const ch = res.success ? res.data : undefined;
  return {
    title: ch ? `Chapter ${ch.chapterNumber}: ${ch.title}` : "Chapter",
    description: ch?.description ?? `Read, summarise and practise this chapter.`,
    alternates: { canonical: `/book/${params.slug}/chapter/${params.chapterId}` },
  };
}

export default async function ChapterPage(
  { params }: { params: { slug: string; chapterId: string } }
) {
  const res = await api.chapter(params.chapterId);
  if (!res.success || !res.data) notFound();
  const chapter = res.data;

  return (
    <div className="container-x py-10">
      <nav aria-label="Breadcrumb" className="mb-3 text-sm text-slate-500">
        <Link href="/" className="hover:text-brand-600">Home</Link> <span aria-hidden>/</span>{" "}
        <Link href={`/book/${params.slug}`} className="hover:text-brand-600">Book</Link>{" "}
        <span aria-hidden>/</span> <span className="text-slate-700">Chapter {chapter.chapterNumber}</span>
      </nav>

      <p className="text-sm font-semibold text-brand-600">Chapter {chapter.chapterNumber}</p>
      <h1 className="text-3xl font-extrabold text-slate-900">{chapter.title}</h1>
      {chapter.titleHi && <p className="mt-1 text-slate-500">{chapter.titleHi}</p>}

      <div className="mt-8">
        <ChapterTabs chapter={chapter} />
      </div>
    </div>
  );
}
