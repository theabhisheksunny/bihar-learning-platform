"use client";

import { Suspense, useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import Link from "next/link";
import { api } from "@/lib/api";
import type { SearchResultDto } from "@/lib/types";
import { SearchBox } from "@/components/SearchBox";
import { EmptyState } from "@/components/EmptyState";

function Results() {
  const params = useSearchParams();
  const q = params.get("q") ?? "";
  const [results, setResults] = useState<SearchResultDto[] | null>(null);

  useEffect(() => {
    if (!q.trim()) {
      setResults([]);
      return;
    }
    setResults(null);
    api.search(q).then((r) => setResults(r.success && r.data ? r.data : []));
  }, [q]);

  return (
    <div className="container-x py-10">
      <h1 className="text-3xl font-extrabold text-slate-900">Search</h1>
      <div className="mt-4 max-w-xl"><SearchBox /></div>

      {q.trim() === "" ? (
        <p className="mt-6 text-slate-600">Type a topic, chapter, or question to search.</p>
      ) : results === null ? (
        <p className="mt-6 text-slate-500">Searching…</p>
      ) : results.length === 0 ? (
        <div className="mt-6">
          <EmptyState title={`No results for “${q}”`} message="Try a different word, or browse by subject from Class 9." />
        </div>
      ) : (
        <ul className="mt-6 space-y-3">
          {results.map((r) => {
            const inner = (
              <>
                <div className="flex items-center justify-between gap-3">
                  <p className="font-semibold text-slate-900">{r.title}</p>
                  <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs capitalize text-slate-600">{r.type}</span>
                </div>
                {r.snippet && <p className="mt-1 text-sm text-slate-600">{r.snippet}</p>}
              </>
            );
            // Books resolve by id on the backend, so we can deep-link them.
            if (r.type === "book") {
              return (
                <li key={`${r.type}-${r.id}`}>
                  <Link href={`/book/${r.id}`} className="card block p-4 hover:shadow-md">{inner}</Link>
                </li>
              );
            }
            return (
              <li key={`${r.type}-${r.id}`} className="card p-4">{inner}</li>
            );
          })}
        </ul>
      )}
    </div>
  );
}

export default function SearchPage() {
  return (
    <Suspense fallback={<div className="container-x py-10 text-slate-500">Loading…</div>}>
      <Results />
    </Suspense>
  );
}
