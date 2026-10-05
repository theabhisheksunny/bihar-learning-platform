import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "About the platform",
  description: "How this Class 9 learning platform works and how it treats content and sources.",
  alternates: { canonical: "/about" },
};

export default function AboutPage() {
  return (
    <div className="container-x prose max-w-3xl py-10">
      <h1 className="text-3xl font-extrabold text-slate-900">About this platform</h1>
      <p className="mt-4 text-slate-700">
        Bihar Class 9 Learning Hub brings together the syllabus, books, chapters, summaries and
        practice questions a Class 9 student needs — in one simple place.
      </p>
      <h2 className="mt-8 text-xl font-bold text-slate-900">How we treat content</h2>
      <ul className="mt-3 list-disc space-y-2 pl-6 text-slate-700">
        <li>Every item shows its source and verification status.</li>
        <li>Official content links to the government or NCERT original; we don’t alter it.</li>
        <li>Study aids generated with AI are clearly labelled and grounded in the chapter.</li>
        <li>Nothing is fabricated. If something isn’t verified, we leave it out or mark it clearly.</li>
      </ul>
      <p className="mt-6 text-sm text-slate-500">
        This is an educational project and not an official government portal.
      </p>
    </div>
  );
}
