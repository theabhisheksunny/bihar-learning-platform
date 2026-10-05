import type { Provenance } from "@/lib/types";

const STATUS_STYLES: Record<string, string> = {
  OFFICIAL: "bg-accent-500/10 text-accent-600 border-accent-500/30",
  VERIFIED: "bg-accent-500/10 text-accent-600 border-accent-500/30",
  THIRD_PARTY: "bg-amber-100 text-amber-700 border-amber-300",
  AI_GENERATED: "bg-violet-100 text-violet-700 border-violet-300",
  PENDING_REVIEW: "bg-slate-100 text-slate-600 border-slate-300",
  OUTDATED: "bg-rose-100 text-rose-700 border-rose-300",
};

const STATUS_LABELS: Record<string, string> = {
  OFFICIAL: "Official source",
  VERIFIED: "Verified",
  THIRD_PARTY: "Third-party source",
  AI_GENERATED: "AI-generated learning aid",
  PENDING_REVIEW: "Pending review",
  OUTDATED: "Outdated",
};

/**
 * Always-visible provenance badge. Students can see where content comes from. Unverified or
 * demo content is clearly marked - we never present it as official.
 */
export function SourceBadge({ provenance }: { provenance?: Provenance | null }) {
  if (!provenance) return null;
  const status = provenance.verificationStatus ?? "PENDING_REVIEW";
  const style = STATUS_STYLES[status] ?? STATUS_STYLES.PENDING_REVIEW;
  const label = STATUS_LABELS[status] ?? status;

  return (
    <div className="flex flex-wrap items-center gap-2 text-sm">
      <span className={`inline-flex items-center rounded-full border px-2.5 py-0.5 font-medium ${style}`}>
        {label}
      </span>
      {provenance.sourceName && (
        <span className="text-slate-600">
          Source:{" "}
          {provenance.sourceUrl ? (
            <a
              className="font-medium text-brand-600 underline underline-offset-2"
              href={provenance.sourceUrl}
              target="_blank"
              rel="noopener noreferrer"
            >
              {provenance.sourceName}
            </a>
          ) : (
            <span className="font-medium">{provenance.sourceName}</span>
          )}
        </span>
      )}
      {provenance.academicYear && (
        <span className="text-slate-500">Academic Year: {provenance.academicYear}</span>
      )}
      {provenance.license && <span className="text-slate-400">License: {provenance.license}</span>}
    </div>
  );
}
