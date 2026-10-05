"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import type { ChapterDetailDto, ContentDto, McqDto, PdfResolutionDto, QuestionDto } from "@/lib/types";
import { SourceBadge } from "@/components/SourceBadge";
import { PdfReader } from "@/components/PdfReader";
import { EmptyState } from "@/components/EmptyState";

type Tab = "overview" | "summary" | "read" | "points" | "questions" | "answers" | "mcqs" | "exercises" | "revision";

const TABS: { key: Tab; label: string }[] = [
  { key: "overview", label: "Overview" },
  { key: "summary", label: "Summary" },
  { key: "read", label: "Read Chapter" },
  { key: "points", label: "Important Points" },
  { key: "questions", label: "Questions" },
  { key: "answers", label: "Answers" },
  { key: "mcqs", label: "MCQs" },
  { key: "exercises", label: "Exercise Solutions" },
  { key: "revision", label: "Revision" },
];

export function ChapterTabs({ chapter }: { chapter: ChapterDetailDto }) {
  const [tab, setTab] = useState<Tab>("overview");

  return (
    <div>
      <div className="overflow-x-auto">
        <div role="tablist" aria-label="Chapter sections" className="flex min-w-max gap-1 border-b border-slate-200">
          {TABS.map((t) => (
            <button
              key={t.key}
              role="tab"
              aria-selected={tab === t.key}
              aria-controls={`panel-${t.key}`}
              id={`tab-${t.key}`}
              onClick={() => setTab(t.key)}
              className={`whitespace-nowrap border-b-2 px-4 py-2.5 text-sm font-medium transition ${
                tab === t.key
                  ? "border-brand-600 text-brand-700"
                  : "border-transparent text-slate-500 hover:text-slate-800"
              }`}
            >
              {t.label}
            </button>
          ))}
        </div>
      </div>

      <div id={`panel-${tab}`} role="tabpanel" aria-labelledby={`tab-${tab}`} className="pt-6">
        {tab === "overview" && <Overview chapter={chapter} />}
        {tab === "summary" && <ContentSection chapterId={chapter.id} type="SUMMARY" emptyLabel="summary" />}
        {tab === "read" && <ReadSection chapterId={chapter.id} />}
        {tab === "points" && <ContentSection chapterId={chapter.id} type="IMPORTANT_POINTS" emptyLabel="important points" />}
        {tab === "questions" && <QuestionsSection chapterId={chapter.id} withAnswers={false} />}
        {tab === "answers" && <QuestionsSection chapterId={chapter.id} withAnswers={true} />}
        {tab === "mcqs" && <McqSection chapterId={chapter.id} />}
        {tab === "exercises" && <ContentSection chapterId={chapter.id} type="EXERCISE" emptyLabel="exercise solutions" />}
        {tab === "revision" && <ContentSection chapterId={chapter.id} type="REVISION" emptyLabel="revision notes" />}
      </div>
    </div>
  );
}

function Overview({ chapter }: { chapter: ChapterDetailDto }) {
  return (
    <div className="space-y-4">
      {chapter.description ? (
        <p className="text-slate-700">{chapter.description}</p>
      ) : (
        <p className="text-slate-600">
          This chapter is part of the official book. Use the tabs to read it, see a summary, or
          practise questions. Study aids are labelled by source.
        </p>
      )}
      <SourceBadge provenance={chapter.provenance} />
    </div>
  );
}

function AiNotice() {
  return (
    <p className="rounded-lg border border-violet-200 bg-violet-50 px-3 py-2 text-sm text-violet-700">
      AI-generated learning aid — a study help grounded in the chapter, not official textbook content.
    </p>
  );
}

function ContentSection({ chapterId, type, emptyLabel }: { chapterId: string; type: string; emptyLabel: string }) {
  const [items, setItems] = useState<ContentDto[] | null>(null);
  useEffect(() => {
    api.chapterContent(chapterId, type).then((r) => setItems(r.success && r.data ? r.data : []));
  }, [chapterId, type]);

  if (items === null) return <p className="text-slate-500">Loading…</p>;
  if (items.length === 0) {
    return (
      <EmptyState
        title={`No ${emptyLabel} yet`}
        message={`We don't show ${emptyLabel} until it is sourced or clearly labelled. Nothing is made up.`}
      />
    );
  }
  return (
    <div className="space-y-4">
      {items.map((c) => (
        <article key={c.id} className="card p-5">
          {c.ai && <div className="mb-3"><AiNotice /></div>}
          <p className="whitespace-pre-wrap text-slate-800">{c.body}</p>
          <div className="mt-3"><SourceBadge provenance={c.provenance} /></div>
        </article>
      ))}
    </div>
  );
}

function ReadSection({ chapterId }: { chapterId: string }) {
  const [pdf, setPdf] = useState<PdfResolutionDto | null | undefined>(undefined);
  useEffect(() => {
    api.chapterPdf(chapterId).then((r) => setPdf(r.success && r.data ? r.data : null));
  }, [chapterId]);

  if (pdf === undefined) return <p className="text-slate-500">Loading…</p>;
  if (!pdf) {
    return (
      <EmptyState
        title="No readable PDF available yet"
        message="A verified official PDF for this chapter is not available. Check the book's official source."
      />
    );
  }
  const badge = (
    <SourceBadge
      provenance={{
        sourceName: pdf.sourceName, sourceUrl: null, sourceType: null, sourceDocumentUrl: pdf.url,
        retrievedAt: null, publishedAt: null, academicYear: null, version: pdf.version,
        license: pdf.license, verificationStatus: pdf.verificationStatus,
      }}
    />
  );

  // Hosted documents (STREAM) are served directly from object storage and are safe to embed.
  // External references (LINK) point at the official source, which often blocks embedding — so we
  // offer a clear "open official source" action instead of silently failing in the reader.
  const isHosted = pdf.mode === "STREAM" && pdf.accessMode !== "EXTERNAL";

  return (
    <div className="space-y-3">
      {badge}
      {isHosted ? (
        <PdfReader url={pdf.url} sourceName={pdf.sourceName} />
      ) : (
        <div className="card p-6 text-center">
          <p className="font-semibold text-slate-800">Read at the official source</p>
          <p className="mx-auto mt-1 max-w-md text-sm text-slate-600">
            {pdf.note ?? "This document is hosted by its publisher. Open it at the official source."}
          </p>
          <a className="btn-primary mt-4" href={pdf.url} target="_blank" rel="noopener noreferrer">
            Open official PDF
          </a>
        </div>
      )}
    </div>
  );
}

function QuestionsSection({ chapterId, withAnswers }: { chapterId: string; withAnswers: boolean }) {
  const [items, setItems] = useState<QuestionDto[] | null>(null);
  const [category, setCategory] = useState<string>("");
  const categories = ["", "MCQ", "VERY_SHORT", "SHORT", "LONG", "IMPORTANT", "PREVIOUS_EXAM"];

  useEffect(() => {
    api.questions(chapterId, category || undefined).then((r) => setItems(r.success && r.data ? r.data : []));
  }, [chapterId, category]);

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap gap-2">
        {categories.map((c) => (
          <button
            key={c || "all"}
            onClick={() => setCategory(c)}
            className={`rounded-full border px-3 py-1 text-sm ${
              category === c ? "border-brand-600 bg-brand-50 text-brand-700" : "border-slate-300 text-slate-600"
            }`}
          >
            {c === "" ? "All" : c.replace("_", " ")}
          </button>
        ))}
      </div>
      {items === null ? (
        <p className="text-slate-500">Loading…</p>
      ) : items.length === 0 ? (
        <EmptyState title="No questions yet" message="Questions appear here once sourced or labelled. Nothing is fabricated." />
      ) : (
        <ol className="space-y-3">
          {items.map((q, i) => (
            <li key={q.id} className="card p-4">
              <div className="flex items-start justify-between gap-3">
                <p className="font-medium text-slate-800">{i + 1}. {q.text}</p>
                <span className="shrink-0 rounded-full bg-slate-100 px-2 py-0.5 text-xs text-slate-600">
                  {q.category.replace("_", " ")}
                </span>
              </div>
              {q.ai && <div className="mt-2"><AiNotice /></div>}
              {withAnswers && q.answers.length > 0 && (
                <div className="mt-3 rounded-lg bg-slate-50 p-3 text-sm text-slate-700">
                  <p className="font-semibold">Answer</p>
                  {q.answers.map((a, idx) => <p key={idx} className="mt-1 whitespace-pre-wrap">{a}</p>)}
                </div>
              )}
            </li>
          ))}
        </ol>
      )}
    </div>
  );
}

function McqSection({ chapterId }: { chapterId: string }) {
  const [items, setItems] = useState<McqDto[] | null>(null);
  const [revealed, setRevealed] = useState<Record<string, boolean>>({});
  useEffect(() => {
    api.mcqs(chapterId).then((r) => setItems(r.success && r.data ? r.data : []));
  }, [chapterId]);

  if (items === null) return <p className="text-slate-500">Loading…</p>;
  if (items.length === 0) {
    return <EmptyState title="No MCQs yet" message="Multiple-choice questions appear once sourced or labelled." />;
  }
  return (
    <ol className="space-y-4">
      {items.map((m, i) => (
        <li key={m.id} className="card p-4">
          <p className="font-medium text-slate-800">{i + 1}. {m.question}</p>
          {m.ai && <div className="mt-2"><AiNotice /></div>}
          <ul className="mt-3 space-y-1">
            {m.options.map((opt, idx) => {
              const show = revealed[m.id];
              const correct = idx === m.correctIndex;
              return (
                <li
                  key={idx}
                  className={`rounded-lg border px-3 py-2 text-sm ${
                    show && correct ? "border-accent-500 bg-accent-500/10 text-accent-600" : "border-slate-200"
                  }`}
                >
                  <span className="mr-2 font-semibold">{String.fromCharCode(65 + idx)}.</span>
                  {opt}
                </li>
              );
            })}
          </ul>
          <button
            className="btn-secondary mt-3 px-3 py-1.5 text-sm"
            onClick={() => setRevealed((r) => ({ ...r, [m.id]: !r[m.id] }))}
          >
            {revealed[m.id] ? "Hide answer" : "Show answer"}
          </button>
          {revealed[m.id] && m.explanation && (
            <p className="mt-2 text-sm text-slate-600">{m.explanation}</p>
          )}
        </li>
      ))}
    </ol>
  );
}
