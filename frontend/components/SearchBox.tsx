"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";

export function SearchBox({ compact = false }: { compact?: boolean }) {
  const router = useRouter();
  const [q, setQ] = useState("");

  function submit(e: React.FormEvent) {
    e.preventDefault();
    const term = q.trim();
    if (term) router.push(`/search?q=${encodeURIComponent(term)}`);
  }

  return (
    <form role="search" onSubmit={submit} className="flex w-full gap-2">
      <label htmlFor={compact ? "search-compact" : "search-main"} className="sr-only">
        Search books, chapters, questions
      </label>
      <input
        id={compact ? "search-compact" : "search-main"}
        type="search"
        value={q}
        onChange={(e) => setQ(e.target.value)}
        placeholder="Search chapters, questions…"
        className="w-full rounded-xl border border-slate-300 px-4 py-2 text-sm
                   focus:border-brand-500 focus:outline-none"
      />
      <button type="submit" className="rounded-xl bg-brand-600 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-700">
        Go
      </button>
    </form>
  );
}
