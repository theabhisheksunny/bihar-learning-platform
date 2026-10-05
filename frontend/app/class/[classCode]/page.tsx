import type { Metadata } from "next";
import Link from "next/link";
import { api } from "@/lib/api";
import { SubjectCard } from "@/components/SubjectCard";
import { EmptyState } from "@/components/EmptyState";

export const revalidate = 60;

export async function generateMetadata(
  { params }: { params: { classCode: string } }
): Promise<Metadata> {
  return {
    title: `Class ${params.classCode} — subjects, books and chapters`,
    description: `Browse Bihar Board Class ${params.classCode} subjects, books, chapters and syllabus.`,
    alternates: { canonical: `/class/${params.classCode}` },
  };
}

export default async function ClassPage({ params }: { params: { classCode: string } }) {
  const [clsRes, subjectsRes] = await Promise.all([
    api.classDetail(params.classCode),
    api.subjects(params.classCode),
  ]);
  const cls = clsRes.success ? clsRes.data : undefined;
  const subjects = subjectsRes.success && subjectsRes.data ? subjectsRes.data : [];

  return (
    <div className="container-x py-10">
      <nav aria-label="Breadcrumb" className="mb-3 text-sm text-slate-500">
        <Link href="/" className="hover:text-brand-600">Home</Link> <span aria-hidden>/</span>{" "}
        <span className="text-slate-700">{cls?.name ?? `Class ${params.classCode}`}</span>
      </nav>
      <h1 className="text-3xl font-extrabold text-slate-900">{cls?.name ?? `Class ${params.classCode}`}</h1>
      <p className="mt-2 text-slate-600">Pick a subject to see its books and chapters.</p>

      <div className="mt-6 flex flex-wrap gap-3">
        <Link href={`/syllabus/${params.classCode}`} className="btn-secondary">View Syllabus</Link>
      </div>

      <h2 className="mt-10 text-xl font-bold text-slate-900">Subjects</h2>
      <div className="mt-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {subjects.length > 0 ? (
          subjects.map((s) => <SubjectCard key={s.id} classCode={params.classCode} subject={s} />)
        ) : (
          <div className="sm:col-span-2 lg:col-span-3">
            <EmptyState
              title="No subjects available yet"
              message="This class may not be configured yet, or the backend is unavailable."
            />
          </div>
        )}
      </div>
    </div>
  );
}
