import type { Metadata } from "next";
import "./globals.css";
import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";

export const metadata: Metadata = {
  metadataBase: new URL(process.env.SITE_URL || "http://localhost:3000"),
  title: {
    default: "Bihar Class 9 Learning Hub",
    template: "%s · Bihar Class 9 Learning Hub",
  },
  description:
    "Books, syllabus, chapters, summaries, questions and solutions for Bihar Board Class 9 students — all in one place.",
  openGraph: {
    title: "Bihar Class 9 Learning Hub",
    description: "Books, syllabus, chapters, summaries, questions and solutions in one place.",
    type: "website",
  },
  robots: { index: true, follow: true },
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>
        <a
          href="#main"
          className="sr-only focus:not-sr-only focus:absolute focus:left-4 focus:top-4 focus:z-50
                     focus:rounded-lg focus:bg-white focus:px-4 focus:py-2 focus:shadow"
        >
          Skip to content
        </a>
        <Header />
        <main id="main">{children}</main>
        <Footer />
      </body>
    </html>
  );
}
