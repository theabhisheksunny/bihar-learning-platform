"use client";

import { useEffect, useState } from "react";
import {
  adminApi, clearToken, getToken, setToken,
  type ChangeRow, type DocumentRow, type SourceRow, type StorageStats, type SyncJobRow,
} from "@/features/admin/adminApi";

function formatBytes(n: number): string {
  if (!n) return "0 B";
  const units = ["B", "KB", "MB", "GB", "TB"];
  const i = Math.floor(Math.log(n) / Math.log(1024));
  return `${(n / Math.pow(1024, i)).toFixed(1)} ${units[i]}`;
}

export default function AdminPage() {
  const [authed, setAuthed] = useState(false);
  useEffect(() => setAuthed(!!getToken()), []);
  return authed ? <Dashboard onLogout={() => { clearToken(); setAuthed(false); }} /> : <Login onLogin={() => setAuthed(true)} />;
}

function Login({ onLogin }: { onLogin: () => void }) {
  const [email, setEmail] = useState("admin@bihar-learning.local");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const { token } = await adminApi.login(email, password);
      setToken(token);
      onLogin();
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="container-x max-w-md py-16">
      <h1 className="text-2xl font-bold text-slate-900">Admin sign in</h1>
      <form onSubmit={submit} className="card mt-6 space-y-4 p-6">
        <div>
          <label htmlFor="email" className="block text-sm font-medium text-slate-700">Email</label>
          <input id="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)}
                 className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2" required />
        </div>
        <div>
          <label htmlFor="password" className="block text-sm font-medium text-slate-700">Password</label>
          <input id="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)}
                 className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2" required />
        </div>
        {error && <p className="text-sm text-rose-600" role="alert">{error}</p>}
        <button type="submit" className="btn-primary w-full" disabled={busy}>
          {busy ? "Signing in…" : "Sign in"}
        </button>
      </form>
      <p className="mt-3 text-xs text-slate-400">
        Dev default: admin@bihar-learning.local / admin12345 (override in production).
      </p>
    </div>
  );
}

function Dashboard({ onLogout }: { onLogout: () => void }) {
  const [stats, setStats] = useState<Record<string, number>>({});
  const [sources, setSources] = useState<SourceRow[]>([]);
  const [jobs, setJobs] = useState<SyncJobRow[]>([]);
  const [review, setReview] = useState<ChangeRow[]>([]);
  const [storage, setStorage] = useState<StorageStats | null>(null);
  const [documents, setDocuments] = useState<DocumentRow[]>([]);
  const [orphans, setOrphans] = useState<DocumentRow[]>([]);
  const [failed, setFailed] = useState<DocumentRow[]>([]);
  const [msg, setMsg] = useState<string | null>(null);

  async function refresh() {
    try {
      const [s, src, j, r, st, docs, orph, fail] = await Promise.all([
        adminApi.stats(), adminApi.sources(), adminApi.jobs(), adminApi.review(),
        adminApi.storageStats().catch(() => null),
        adminApi.storageDocuments().catch(() => [] as DocumentRow[]),
        adminApi.storageOrphans().catch(() => [] as DocumentRow[]),
        adminApi.storageFailed().catch(() => [] as DocumentRow[]),
      ]);
      setStats(s); setSources(src); setJobs(j); setReview(r);
      setStorage(st); setDocuments(docs); setOrphans(orph); setFailed(fail);
    } catch (e) {
      setMsg((e as Error).message);
    }
  }
  useEffect(() => { refresh(); }, []);

  async function doSync(id: string) {
    setMsg(null);
    try {
      await adminApi.sync(id);
      setMsg("Sync triggered.");
      refresh();
    } catch (e) {
      setMsg((e as Error).message);
    }
  }

  async function act(id: string, kind: "approve" | "reject") {
    try {
      if (kind === "approve") await adminApi.approve(id); else await adminApi.reject(id);
      refresh();
    } catch (e) { setMsg((e as Error).message); }
  }

  const statCards = [
    { k: "books", label: "Books" }, { k: "subjects", label: "Subjects" },
    { k: "chapters", label: "Chapters" }, { k: "sources", label: "Sources" },
    { k: "syncSuccess", label: "Sync OK" }, { k: "syncFailed", label: "Sync failed" },
    { k: "pendingReview", label: "Pending review" },
  ];

  return (
    <div className="container-x py-10">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-slate-900">Admin dashboard</h1>
        <button className="btn-secondary px-4 py-2 text-sm" onClick={onLogout}>Sign out</button>
      </div>
      {msg && <p className="mt-3 rounded-lg bg-brand-50 px-3 py-2 text-sm text-brand-700" role="status">{msg}</p>}

      <div className="mt-6 grid grid-cols-2 gap-3 sm:grid-cols-4 lg:grid-cols-7">
        {statCards.map((c) => (
          <div key={c.k} className="card p-4 text-center">
            <p className="text-2xl font-extrabold text-slate-900">{stats[c.k] ?? 0}</p>
            <p className="text-xs text-slate-500">{c.label}</p>
          </div>
        ))}
      </div>

      <StorageSection storage={storage} documents={documents} orphans={orphans} failed={failed} />

      <section className="mt-10">
        <h2 className="text-lg font-bold text-slate-900">Content sources</h2>
        <div className="mt-3 overflow-x-auto">
          <table className="w-full min-w-[640px] border-collapse text-sm">
            <thead>
              <tr className="border-b border-slate-200 text-left text-slate-500">
                <th className="py-2 pr-4">Source</th><th className="py-2 pr-4">Tier</th>
                <th className="py-2 pr-4">Enabled</th><th className="py-2 pr-4">robots confirmed</th>
                <th className="py-2 pr-4">Last success</th><th className="py-2">Action</th>
              </tr>
            </thead>
            <tbody>
              {sources.map((s) => (
                <tr key={s.id} className="border-b border-slate-100">
                  <td className="py-2 pr-4 font-medium text-slate-800">{s.name}</td>
                  <td className="py-2 pr-4">T{s.authorityTier}</td>
                  <td className="py-2 pr-4">{s.enabled ? "Yes" : "No"}</td>
                  <td className="py-2 pr-4">{s.robotsConfirmed ? "Yes" : "No"}</td>
                  <td className="py-2 pr-4">{s.lastSuccessAt ?? "—"}</td>
                  <td className="py-2">
                    <button className="btn-secondary px-3 py-1 text-xs" onClick={() => doSync(s.id)}>Sync Now</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <p className="mt-2 text-xs text-slate-400">
          Sync is disabled globally by default and per source until robots.txt / terms are confirmed.
        </p>
      </section>

      <section className="mt-10 grid gap-8 lg:grid-cols-2">
        <div>
          <h2 className="text-lg font-bold text-slate-900">Review queue</h2>
          {review.length === 0 ? (
            <p className="mt-3 text-sm text-slate-500">Nothing pending.</p>
          ) : (
            <ul className="mt-3 space-y-2">
              {review.map((c) => (
                <li key={c.id} className="card flex items-center justify-between p-3 text-sm">
                  <span>{c.changeType} · {c.contentTable}</span>
                  <span className="flex gap-2">
                    <button className="btn-secondary px-3 py-1 text-xs" onClick={() => act(c.id, "approve")}>Approve</button>
                    <button className="rounded-lg border border-rose-300 px-3 py-1 text-xs text-rose-600" onClick={() => act(c.id, "reject")}>Reject</button>
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>
        <div>
          <h2 className="text-lg font-bold text-slate-900">Recent sync jobs</h2>
          {jobs.length === 0 ? (
            <p className="mt-3 text-sm text-slate-500">No jobs yet.</p>
          ) : (
            <ul className="mt-3 space-y-2">
              {jobs.slice(0, 10).map((j) => (
                <li key={j.id} className="card p-3 text-sm">
                  <span className="font-medium">{j.status}</span> · discovered {j.recordsDiscovered},
                  updated {j.recordsUpdated}, failed {j.recordsFailed}
                </li>
              ))}
            </ul>
          )}
        </div>
      </section>
    </div>
  );
}


function StorageSection({
  storage, documents, orphans, failed,
}: {
  storage: StorageStats | null;
  documents: DocumentRow[];
  orphans: DocumentRow[];
  failed: DocumentRow[];
}) {
  const levelClass =
    storage?.level === "CRITICAL" ? "bg-rose-50 text-rose-700 border-rose-200"
    : storage?.level === "WARNING" ? "bg-amber-50 text-amber-700 border-amber-200"
    : "bg-slate-50 text-slate-600 border-slate-200";

  const cards = storage
    ? [
        { label: "Used", value: formatBytes(storage.totalBytes) },
        { label: "Documents", value: String(storage.totalFiles) },
        { label: "PDFs", value: `${storage.pdfCount} · ${formatBytes(storage.pdfBytes)}` },
        { label: "Images", value: `${storage.imageCount} · ${formatBytes(storage.imageBytes)}` },
        { label: "Archived", value: formatBytes(storage.archivedBytes) },
        { label: "External", value: String(storage.externalDocuments) },
        { label: "Failed", value: String(storage.failedDocuments) },
        { label: "Provider", value: storage.activeProvider },
      ]
    : [];

  return (
    <section className="mt-10">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h2 className="text-lg font-bold text-slate-900">Storage</h2>
        {storage && (
          <span className={`rounded-full border px-3 py-1 text-xs font-medium ${levelClass}`}>
            Usage: {storage.usagePercent === "UNKNOWN" ? "UNKNOWN" : `${storage.usagePercent}%`}
            {" · "}Quota: {storage.quota === "UNKNOWN" ? "UNKNOWN" : formatBytes(Number(storage.quota))}
            {" · "}{storage.level}
          </span>
        )}
      </div>

      {!storage ? (
        <p className="mt-3 text-sm text-slate-500">Storage metrics unavailable.</p>
      ) : (
        <>
          <div className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-4 lg:grid-cols-8">
            {cards.map((c) => (
              <div key={c.label} className="card p-4 text-center">
                <p className="text-base font-extrabold text-slate-900">{c.value}</p>
                <p className="text-xs text-slate-500">{c.label}</p>
              </div>
            ))}
          </div>
          <p className="mt-2 text-xs text-slate-400">
            Usage/quota are reported as UNKNOWN unless a provider quota is configured — we never
            invent storage figures. Binaries live in object storage; PostgreSQL stores only metadata.
          </p>

          <div className="mt-6">
            <h3 className="text-sm font-bold text-slate-800">Documents</h3>
            {documents.length === 0 ? (
              <p className="mt-2 text-sm text-slate-500">No documents yet.</p>
            ) : (
              <div className="mt-2 overflow-x-auto">
                <table className="w-full min-w-[760px] border-collapse text-sm">
                  <thead>
                    <tr className="border-b border-slate-200 text-left text-slate-500">
                      <th className="py-2 pr-4">File</th>
                      <th className="py-2 pr-4">Provider</th>
                      <th className="py-2 pr-4">Hosted</th>
                      <th className="py-2 pr-4">Permission</th>
                      <th className="py-2 pr-4">Access</th>
                      <th className="py-2 pr-4">Ver.</th>
                      <th className="py-2 pr-4">Size</th>
                      <th className="py-2 pr-4">Status</th>
                      <th className="py-2">Source</th>
                    </tr>
                  </thead>
                  <tbody>
                    {documents.map((d) => (
                      <tr key={d.id} className="border-b border-slate-100">
                        <td className="py-2 pr-4 font-medium text-slate-800">{d.fileName ?? "—"}</td>
                        <td className="py-2 pr-4">{d.storageProvider}</td>
                        <td className="py-2 pr-4">{d.hosted ? "Yes" : "No"}</td>
                        <td className="py-2 pr-4">{d.hostingPermission}</td>
                        <td className="py-2 pr-4">{d.accessMode}</td>
                        <td className="py-2 pr-4">v{d.version}</td>
                        <td className="py-2 pr-4">{d.fileSize ? formatBytes(d.fileSize) : "—"}</td>
                        <td className="py-2 pr-4">{d.status}</td>
                        <td className="py-2">
                          {d.officialSourceUrl || d.sourceUrl ? (
                            <a className="text-brand-600 underline"
                               href={(d.officialSourceUrl ?? d.sourceUrl) as string}
                               target="_blank" rel="noopener noreferrer">
                              Open source
                            </a>
                          ) : "—"}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          <div className="mt-6 grid gap-8 lg:grid-cols-2">
            <div>
              <h3 className="text-sm font-bold text-slate-800">
                Orphaned files <span className="text-slate-400">({orphans.length})</span>
              </h3>
              {orphans.length === 0 ? (
                <p className="mt-2 text-sm text-slate-500">No deletion candidates.</p>
              ) : (
                <ul className="mt-2 space-y-2">
                  {orphans.map((d) => (
                    <li key={d.id} className="card flex items-center justify-between p-3 text-sm">
                      <span>{d.fileName ?? d.documentHash.slice(0, 12)}</span>
                      <span className="rounded-full bg-amber-50 px-2 py-0.5 text-xs text-amber-700">
                        candidate for deletion
                      </span>
                    </li>
                  ))}
                </ul>
              )}
            </div>
            <div>
              <h3 className="text-sm font-bold text-slate-800">
                Failed files <span className="text-slate-400">({failed.length})</span>
              </h3>
              {failed.length === 0 ? (
                <p className="mt-2 text-sm text-slate-500">No failed downloads.</p>
              ) : (
                <ul className="mt-2 space-y-2">
                  {failed.map((d) => (
                    <li key={d.id} className="card p-3 text-sm">
                      <span className="font-medium">{d.fileName ?? d.documentHash.slice(0, 12)}</span>
                      {" · "}
                      <span className="text-rose-600">{d.status}</span>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        </>
      )}
    </section>
  );
}
