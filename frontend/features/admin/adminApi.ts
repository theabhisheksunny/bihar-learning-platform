"use client";

// Admin API client (browser only). Token kept in localStorage for this lightweight panel.

const BASE = "/api/v1/admin";
const TOKEN_KEY = "bihar_admin_token";

export function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(TOKEN_KEY);
}
export function setToken(t: string) {
  localStorage.setItem(TOKEN_KEY, t);
}
export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

async function req<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getToken();
  const res = await fetch(`${BASE}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers ?? {}),
    },
  });
  const body = await res.json().catch(() => null);
  if (!res.ok || (body && body.success === false)) {
    const msg = body?.error?.message ?? res.statusText;
    throw new Error(msg);
  }
  return body.data as T;
}

export interface SourceRow {
  id: string;
  sourceKey: string;
  name: string;
  authorityTier: number;
  enabled: boolean;
  robotsConfirmed: boolean;
  lastCheckedAt: string | null;
  lastSuccessAt: string | null;
}
export interface SyncJobRow {
  id: string;
  status: string;
  recordsDiscovered: number;
  recordsUpdated: number;
  recordsFailed: number;
  startedAt: string | null;
  finishedAt: string | null;
}
export interface ChangeRow {
  id: string;
  contentTable: string;
  changeType: string;
  status: string;
  detectedAt: string;
}

export interface StorageStats {
  totalFiles: number;
  totalBytes: number;
  pdfCount: number;
  pdfBytes: number;
  imageCount: number;
  imageBytes: number;
  currentVersionBytes: number;
  archivedBytes: number;
  externalDocuments: number;
  quota: string;         // "UNKNOWN" or bytes
  usagePercent: string;  // "UNKNOWN" or percent
  level: string;         // OK | WARNING | CRITICAL | UNKNOWN
  candidatesForDeletion: number;
  failedDocuments: number;
  activeProvider: string;
}

export interface DocumentRow {
  id: string;
  documentHash: string;
  fileName: string | null;
  mimeType: string | null;
  fileSize: number | null;
  storageProvider: string;
  hosted: boolean;
  hostingPermission: string;
  accessMode: string;
  version: number;
  status: string;
  candidateForDeletion: boolean;
  sourceUrl: string | null;
  officialSourceUrl: string | null;
}

export const adminApi = {
  login: (email: string, password: string) =>
    req<{ token: string; role: string }>("/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    }),
  stats: () => req<Record<string, number>>("/stats"),
  sources: () => req<SourceRow[]>("/sources"),
  sync: (id: string) => req<SyncJobRow>(`/sources/${id}/sync`, { method: "POST" }),
  jobs: () => req<SyncJobRow[]>("/sync-jobs"),
  review: () => req<ChangeRow[]>("/review"),
  approve: (id: string) => req<ChangeRow>(`/review/${id}/approve`, { method: "POST" }),
  reject: (id: string) => req<ChangeRow>(`/review/${id}/reject`, { method: "POST" }),
  // Storage dashboard
  storageStats: () => req<StorageStats>("/storage/stats"),
  storageDocuments: () => req<DocumentRow[]>("/storage/documents"),
  storageOrphans: () => req<DocumentRow[]>("/storage/orphans"),
  storageFailed: () => req<DocumentRow[]>("/storage/failed"),
};
