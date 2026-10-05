import Link from "next/link";
import { SearchBox } from "./SearchBox";

export function Header() {
  return (
    <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/90 backdrop-blur">
      <div className="container-x flex h-16 items-center justify-between gap-4">
        <Link href="/" className="flex items-center gap-2 font-bold text-brand-700">
          <span aria-hidden className="grid h-9 w-9 place-items-center rounded-xl bg-brand-600 text-white">ब</span>
          <span className="hidden sm:inline">Bihar Class 9 Learning Hub</span>
          <span className="sm:hidden">Class 9 Hub</span>
        </Link>
        <nav aria-label="Primary" className="hidden items-center gap-6 text-sm font-medium md:flex">
          <Link href="/" className="hover:text-brand-600">Home</Link>
          <Link href="/class/9" className="hover:text-brand-600">Class 9</Link>
          <Link href="/syllabus/9" className="hover:text-brand-600">Syllabus</Link>
          <Link href="/search" className="hover:text-brand-600">Search</Link>
        </nav>
        <div className="hidden w-64 lg:block">
          <SearchBox compact />
        </div>
      </div>
    </header>
  );
}
