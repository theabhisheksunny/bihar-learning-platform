import Link from "next/link";

export function Footer() {
  return (
    <footer className="mt-16 border-t border-slate-200 bg-white">
      <div className="container-x grid gap-6 py-10 sm:grid-cols-3">
        <div>
          <p className="font-bold text-brand-700">Bihar Class 9 Learning Hub</p>
          <p className="mt-2 text-sm text-slate-600">
            A single place to find Class 9 books, syllabus, chapters, summaries and questions.
          </p>
        </div>
        <nav aria-label="Footer" className="text-sm">
          <p className="font-semibold text-slate-800">Explore</p>
          <ul className="mt-2 space-y-1 text-slate-600">
            <li><Link href="/class/9" className="hover:text-brand-600">Class 9</Link></li>
            <li><Link href="/syllabus/9" className="hover:text-brand-600">Syllabus</Link></li>
            <li><Link href="/search" className="hover:text-brand-600">Search</Link></li>
            <li><Link href="/about" className="hover:text-brand-600">About</Link></li>
          </ul>
        </nav>
        <div className="text-sm text-slate-600">
          <p className="font-semibold text-slate-800">Content &amp; sources</p>
          <p className="mt-2">
            Content belongs to its publishers (NCERT, Bihar Education Dept, SCERT). We show the
            source on every page and link to the official document. AI learning aids are clearly
            labelled and never presented as official content.
          </p>
        </div>
      </div>
      <div className="border-t border-slate-100 py-4 text-center text-xs text-slate-400">
        Educational project · Not an official government portal.
      </div>
    </footer>
  );
}
