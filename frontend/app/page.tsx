import Link from "next/link";
import { api } from "@/lib/api";
import { SubjectCard } from "@/components/SubjectCard";
import { EmptyState } from "@/components/EmptyState";

export const revalidate = 60;

export default async function HomePage() {
  const subjectsRes = await api.subjects("9");
  const subjects = subjectsRes.success && subjectsRes.data ? subjectsRes.data : [];

  const quickActions = [
    { href: "/class/9", label: "Books", emoji: "📚" },
    { href: "/class/9", label: "Chapters", emoji: "📖" },
    { href: "/search", label: "Questions", emoji: "📝" },
    { href: "/class/9", label: "Summaries", emoji: "💡" },
    { href: "/syllabus/9", label: "Syllabus", emoji: "📋" },
  ];

  return (
    <>
      {/* Hero */}
      <section className="bg-gradient-to-b from-brand-50 to-slate-50">
        <div className="container-x py-14 text-center sm:py-20">
          <p className="mb-3 inline-block rounded-full bg-brand-100 px-3 py-1 text-sm font-medium text-brand-700">
            For Bihar Board Class 9 students
          </p>
          <h1 className="text-balance text-4xl font-extrabold tracking-tight text-slate-900 sm:text-5xl">
            Bihar Class 9 Learning Hub
          </h1>
          <p className="mx-auto mt-4 max-w-2xl text-lg text-slate-600">
            Books, syllabus, chapters, summaries, questions and solutions — all in one place.
          </p>
          <div className="mt-8 flex flex-wrap justify-center gap-3">
            <Link href="/class/9" className="btn-primary">Explore Class 9</Link>
            <Link href="/syllabus/9" className="btn-secondary">View Syllabus</Link>
          </div>
        </div>
      </section>

      {/* Quick actions */}
      <section className="container-x py-10" aria-labelledby="quick-actions">
        <h2 id="quick-actions" className="text-xl font-bold text-slate-900">What do you want to learn?</h2>
        <div className="mt-5 grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-5">
          {quickActions.map((a) => (
            <Link key={a.label} href={a.href} className="card flex flex-col items-center gap-2 p-5 text-center">
              <span aria-hidden className="text-3xl">{a.emoji}</span>
              <span className="font-semibold text-slate-800">{a.label}</span>
            </Link>
          ))}
        </div>
      </section>

      {/* Subjects */}
      <section className="container-x py-6" aria-labelledby="subjects">
        <div className="flex items-baseline justify-between">
          <h2 id="subjects" className="text-xl font-bold text-slate-900">Class 9 Subjects</h2>
          <Link href="/class/9" className="text-sm font-semibold text-brand-600 hover:underline">View all →</Link>
        </div>
        <div className="mt-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {subjects.length > 0 ? (
            subjects.map((s) => <SubjectCard key={s.id} classCode="9" subject={s} />)
          ) : (
            <div className="sm:col-span-2 lg:col-span-3">
              <EmptyState
                title="Subjects are loading or unavailable"
                message="If this persists, the backend may not be running yet. Start the API and refresh."
              />
            </div>
          )}
        </div>
      </section>

      {/* About */}
      <section className="container-x py-10" aria-labelledby="about">
        <div className="card p-6">
          <h2 id="about" className="text-lg font-bold text-slate-900">About this platform</h2>
          <p className="mt-2 max-w-3xl text-slate-600">
            This is a student-friendly place to discover Class 9 learning material. Every item shows
            its source. Official content links to the government or NCERT original; study aids are
            clearly labelled and never presented as official textbook content.
          </p>
        </div>
      </section>
    </>
  );
}
