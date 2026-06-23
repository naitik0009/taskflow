import type { User } from '../types';
import Avatar from './Avatar';

interface PresenceBarProps {
  connected: boolean;
  viewers: User[];
}

export default function PresenceBar({ connected, viewers }: PresenceBarProps) {
  return (
    <div className="flex items-center gap-2">
      <span
        title={connected ? 'Live — real-time sync active' : 'Connecting…'}
        className={`inline-block h-2.5 w-2.5 rounded-full ${
          connected ? 'animate-pulse bg-emerald-500' : 'bg-slate-300'
        }`}
      />
      <span className="hidden text-xs font-medium text-slate-500 sm:inline">
        {connected ? 'Live' : 'Offline'}
      </span>
      {viewers.length > 0 && (
        <div className="ml-1 flex -space-x-2">
          {viewers.slice(0, 5).map((user) => (
            <Avatar key={user.id} user={user} size="sm" ring />
          ))}
          {viewers.length > 5 && (
            <span className="inline-flex h-7 w-7 items-center justify-center rounded-full bg-slate-300 text-[11px] font-semibold text-slate-600 ring-2 ring-white">
              +{viewers.length - 5}
            </span>
          )}
        </div>
      )}
    </div>
  );
}
