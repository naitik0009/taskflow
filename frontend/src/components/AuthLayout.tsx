import type { ReactNode } from 'react';

interface AuthLayoutProps {
  title: string;
  subtitle: string;
  children: ReactNode;
}

export default function AuthLayout({ title, subtitle, children }: AuthLayoutProps) {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-brand-600 to-brand-800 p-4">
      <div className="w-full max-w-md">
        <div className="mb-6 text-center text-white">
          <div className="mb-2 inline-flex items-center gap-2 text-2xl font-extrabold tracking-tight">
            <span className="inline-block h-7 w-7 rounded-md bg-white/90" />
            TaskFlow
          </div>
          <p className="text-sm text-brand-100">Real-time collaborative Kanban</p>
        </div>
        <div className="rounded-2xl bg-white p-7 shadow-2xl">
          <h1 className="mb-1 text-xl font-bold text-slate-800">{title}</h1>
          <p className="mb-5 text-sm text-slate-500">{subtitle}</p>
          {children}
        </div>
      </div>
    </div>
  );
}
