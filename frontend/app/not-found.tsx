import Link from "next/link";

export default function NotFound() {
  return (
    <div className="container-x py-20 text-center">
      <p className="text-6xl">🔎</p>
      <h1 className="mt-4 text-2xl font-bold text-slate-900">Page not found</h1>
      <p className="mt-2 text-slate-600">The page you’re looking for doesn’t exist or has moved.</p>
      <Link href="/" className="btn-primary mt-6">Back to home</Link>
    </div>
  );
}
