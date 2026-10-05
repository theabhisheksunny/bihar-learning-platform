export function EmptyState({ title, message }: { title: string; message: string }) {
  return (
    <div className="card p-8 text-center">
      <p className="text-lg font-semibold text-slate-800">{title}</p>
      <p className="mx-auto mt-2 max-w-md text-sm text-slate-600">{message}</p>
    </div>
  );
}
