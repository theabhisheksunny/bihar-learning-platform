import type { MetadataRoute } from "next";
import { api } from "@/lib/api";

/**
 * Dynamic sitemap. Static routes plus Class 9 subject pages (and books where reachable).
 * Degrades gracefully if the backend is unavailable at build time.
 */
export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  const base = process.env.SITE_URL || "http://localhost:3000";
  const now = new Date();

  const entries: MetadataRoute.Sitemap = [
    { url: `${base}/`, lastModified: now, priority: 1 },
    { url: `${base}/class/9`, lastModified: now, priority: 0.9 },
    { url: `${base}/syllabus/9`, lastModified: now, priority: 0.7 },
    { url: `${base}/about`, lastModified: now, priority: 0.3 },
  ];

  try {
    const res = await api.subjects("9");
    if (res.success && res.data) {
      for (const s of res.data) {
        entries.push({ url: `${base}/class/9/${s.code}`, lastModified: now, priority: 0.7 });
      }
    }
  } catch {
    // ignore - sitemap still valid with static routes
  }
  return entries;
}
