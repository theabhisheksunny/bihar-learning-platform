"use client";

import { useEffect, useState } from "react";

/**
 * Lightweight, accessible PDF viewer. Loads pdf.js lazily so it does not bloat the initial
 * bundle. Handles loading / error / retry states and lets the student open the official source
 * in a new tab. Zoom, page navigation and keyboard shortcuts included.
 *
 * We deliberately render the official document unmodified (attribution shown by the caller).
 */
export function PdfReader({ url, sourceName }: { url: string; sourceName?: string | null }) {
  const [status, setStatus] = useState<"loading" | "ready" | "error">("loading");
  const [doc, setDoc] = useState<any>(null);
  const [page, setPage] = useState(1);
  const [numPages, setNumPages] = useState(0);
  const [scale, setScale] = useState(1.1);
  const [attempt, setAttempt] = useState(0);
  const [query, setQuery] = useState("");
  const [searchMsg, setSearchMsg] = useState<string | null>(null);
  const [fullscreen, setFullscreen] = useState(false);

  useEffect(() => {
    let cancelled = false;
    setStatus("loading");

    async function load() {
      try {
        const pdfjs = await import("pdfjs-dist");
        // Worker from the matching version on a CDN (no bundler worker config needed).
        pdfjs.GlobalWorkerOptions.workerSrc =
          `https://cdnjs.cloudflare.com/ajax/libs/pdf.js/${pdfjs.version}/pdf.worker.min.mjs`;
        const task = pdfjs.getDocument({ url });
        const loaded = await task.promise;
        if (cancelled) return;
        setDoc(loaded);
        setNumPages(loaded.numPages);
        setPage(1);
        setStatus("ready");
      } catch {
        if (!cancelled) setStatus("error");
      }
    }
    load();
    return () => {
      cancelled = true;
    };
  }, [url, attempt]);

  useEffect(() => {
    if (status !== "ready" || !doc) return;
    let cancelled = false;
    (async () => {
      try {
        const p = await doc.getPage(page);
        const viewport = p.getViewport({ scale });
        const canvas = document.getElementById("pdf-canvas") as HTMLCanvasElement | null;
        if (!canvas || cancelled) return;
        const ctx = canvas.getContext("2d");
        if (!ctx) return;
        canvas.height = viewport.height;
        canvas.width = viewport.width;
        await p.render({ canvasContext: ctx, viewport }).promise;
      } catch {
        if (!cancelled) setStatus("error");
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [doc, page, scale, status]);

  function onKey(e: React.KeyboardEvent) {
    if (e.key === "ArrowRight") setPage((p) => Math.min(p + 1, numPages));
    if (e.key === "ArrowLeft") setPage((p) => Math.max(p - 1, 1));
    if (e.key === "+") setScale((s) => Math.min(s + 0.2, 3));
    if (e.key === "-") setScale((s) => Math.max(s - 0.2, 0.5));
  }

  // In-document search: scan pages' text content and jump to the first match (docs section 13).
  async function runSearch(e: React.FormEvent) {
    e.preventDefault();
    const term = query.trim().toLowerCase();
    if (!doc || !term) return;
    setSearchMsg("Searching…");
    try {
      for (let i = 1; i <= numPages; i++) {
        const p = await doc.getPage(i);
        const text = await p.getTextContent();
        const joined = text.items.map((it: any) => it.str).join(" ").toLowerCase();
        if (joined.includes(term)) {
          setPage(i);
          setSearchMsg(`Found on page ${i}`);
          return;
        }
      }
      setSearchMsg("No matches found");
    } catch {
      setSearchMsg("Search unavailable for this document");
    }
  }

  // Fullscreen toggle using the browser Fullscreen API (docs section 13).
  async function toggleFullscreen() {
    const el = document.getElementById("pdf-reader-region");
    try {
      if (!document.fullscreenElement && el) {
        await el.requestFullscreen();
        setFullscreen(true);
      } else if (document.fullscreenElement) {
        await document.exitFullscreen();
        setFullscreen(false);
      }
    } catch {
      /* fullscreen may be blocked; non-fatal */
    }
  }

  useEffect(() => {
    function onFsChange() {
      setFullscreen(!!document.fullscreenElement);
    }
    document.addEventListener("fullscreenchange", onFsChange);
    return () => document.removeEventListener("fullscreenchange", onFsChange);
  }, []);

  if (status === "error") {
    return (
      <div className="card p-6 text-center">
        <p className="font-semibold text-slate-800">We couldn’t load this PDF in the reader</p>
        <p className="mt-1 text-sm text-slate-600">
          The source may block embedding. You can retry or open it directly.
        </p>
        <div className="mt-4 flex justify-center gap-3">
          <button className="btn-secondary" onClick={() => setAttempt((a) => a + 1)}>Retry</button>
          <a className="btn-primary" href={url} target="_blank" rel="noopener noreferrer">
            Open official source
          </a>
        </div>
      </div>
    );
  }

  return (
    <div
      className="card p-3"
      id="pdf-reader-region"
      role="region"
      aria-label={`PDF reader${sourceName ? " for " + sourceName : ""}`}
      tabIndex={0}
      onKeyDown={onKey}
    >
      <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <button
            className="btn-secondary px-3 py-1.5 text-sm"
            onClick={() => setPage((p) => Math.max(p - 1, 1))}
            disabled={page <= 1}
            aria-label="Previous page"
          >
            ← Prev
          </button>
          <span className="text-sm text-slate-600" aria-live="polite">
            Page {page}{numPages ? ` of ${numPages}` : ""}
          </span>
          <button
            className="btn-secondary px-3 py-1.5 text-sm"
            onClick={() => setPage((p) => Math.min(p + 1, numPages))}
            disabled={numPages > 0 && page >= numPages}
            aria-label="Next page"
          >
            Next →
          </button>
        </div>
        <div className="flex items-center gap-2">
          <button className="btn-secondary px-3 py-1.5 text-sm" onClick={() => setScale((s) => Math.max(s - 0.2, 0.5))} aria-label="Zoom out">−</button>
          <span className="text-sm text-slate-600">{Math.round(scale * 100)}%</span>
          <button className="btn-secondary px-3 py-1.5 text-sm" onClick={() => setScale((s) => Math.min(s + 0.2, 3))} aria-label="Zoom in">+</button>
          <button className="btn-secondary px-3 py-1.5 text-sm" onClick={toggleFullscreen} aria-label="Toggle fullscreen">
            {fullscreen ? "Exit full screen" : "Full screen"}
          </button>
          <a className="btn-secondary px-3 py-1.5 text-sm" href={url} target="_blank" rel="noopener noreferrer">Open source</a>
        </div>
      </div>

      <form onSubmit={runSearch} className="mb-3 flex items-center gap-2" role="search">
        <input
          type="search"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Search in document…"
          aria-label="Search in document"
          className="w-full max-w-xs rounded-lg border border-slate-300 px-3 py-1.5 text-sm"
        />
        <button type="submit" className="btn-secondary px-3 py-1.5 text-sm">Search</button>
        {searchMsg && <span className="text-xs text-slate-500" aria-live="polite">{searchMsg}</span>}
      </form>

      <div className="max-h-[75vh] overflow-auto rounded-lg bg-slate-100 p-3 text-center">
        {status === "loading" && (
          <div className="py-16 text-slate-500" aria-live="polite">Loading PDF…</div>
        )}
        <canvas id="pdf-canvas" className={status === "ready" ? "mx-auto inline-block" : "hidden"} />
      </div>
      <p className="mt-2 text-xs text-slate-400">
        Tip: use ← → to change pages and + / − to zoom.
      </p>
    </div>
  );
}
