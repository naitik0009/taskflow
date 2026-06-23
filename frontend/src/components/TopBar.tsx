import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Avatar from './Avatar';
import type { ReactNode } from 'react';

export default function TopBar({ children }: { children?: ReactNode }) {
  const { user, logout } = useAuth();
  return (
    <header className="sticky top-0 z-30 flex h-14 items-center justify-between border-b border-slate-200 bg-white px-4 shadow-sm">
      <div className="flex items-center gap-4">
        <Link to="/" className="flex items-center gap-2 text-lg font-extrabold tracking-tight text-brand-700">
          <span className="inline-block h-6 w-6 rounded-md bg-brand-600" />
          TaskFlow
        </Link>
        {children}
      </div>
      <div className="flex items-center gap-3">
        {user && (
          <>
            <div className="hidden items-center gap-2 sm:flex">
              <Avatar user={user} size="sm" />
              <span className="text-sm font-medium text-slate-600">{user.displayName}</span>
            </div>
            <button
              onClick={logout}
              className="rounded-lg px-3 py-1.5 text-sm font-medium text-slate-500 hover:bg-slate-100 hover:text-slate-700"
            >
              Sign out
            </button>
          </>
        )}
      </div>
    </header>
  );
}
