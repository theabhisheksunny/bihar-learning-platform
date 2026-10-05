import type { Metadata } from "next";
import Link from "next/link";
import { api } from "@/lib/api";
import { SourceBadge } from "@/components/SourceBadge";
import { EmptyState } from "@/components/EmptyState";

export const revalidate = 60;

export async function generateMetadata(
  { params }: { params: { classCode: string } }
): Promise<Metadata> {
  return {
    title: `Class ${params.classCode} Syllabus`,
    description: `Official Bihar Board Class ${params.classCode} syllabus and curriculum.`,
    alternates: { canonical: `/syllabus/${params.classCode}` },
  };
}

export default async function SyllabusPage({ params }: { params: { classCode: string } }) {
  const res = await api.syllabus(params.classCode);
  const items = res.success && res.data ? res.data : [];

  return (
    <div className="container-x py-10">
      <nav aria-label="Breadcrumb" className="mb-3 text-sm text-slate-500">
        <Link href="/" className="hover:text-brand-600">Home</Link> <span aria-hidden>/</span>{" "}
        <span className="text-slate-700">Syllabus · Class {params.classCode}</span>
      </nav>
      <h1 className="text-3xl font-extrabold text-slate-900">Class {params.classCode} Syllabus</h1>
      <p className="mt-2 text-slate-600">Official curriculum. Open the source to view the authoritative document.</p>

      <div className="mt-6 space-y-4">
        {items.length > 0 ? (
          items.map((s) => (
            <article key={s.id} className="card p-5">
              <h2 className="text-lg font-semibold text-slate-900">{s.title}</h2>
              {s.body && <p className="mt-2 text-slate-600">{s.body}</p>}
              {s.documentUrl && (
                <a href={s.documentUrl} target="_blank" rel="noopener noreferrer" className="btn-secondary mt-3">
                  Open official document
                </a>
              )}
              <div className="mt-3">
                <SourceBadge
                  provenance={{
                    sourceName: s.sourceName, sourceUrl: s.sourceUrl, sourceType: null,
                    sourceDocumentUrl: s.documentUrl, retrievedAt: null, publishedAt: null,
                    academicYear: s.academicYear, version: null, license: null,
                    verificationStatus: s.verificationStatus,
                  }}
                />
              </div>
            </article>
          ))
        ) : (
          <EmptyState title="No syllabus available yet" message="The syllabus for this class has not been added yet." />
        )}
      </div>
    </div>
  );
}
