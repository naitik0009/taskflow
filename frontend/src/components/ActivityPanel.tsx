import { useQuery } from '@tanstack/react-query';
import { boardApi } from '../api/endpoints';
import type { BoardMember } from '../types';
import Avatar from './Avatar';
import Spinner from './Spinner';

interface ActivityPanelProps {
  boardId: string;
  members: BoardMember[];
  refreshKey: number;
}

function timeAgo(iso: string): string {
  const diff = Date.now() - new Date(iso).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return 'just now';
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  return `${Math.floor(hrs / 24)}d ago`;
}

export default function ActivityPanel({ boardId, members, refreshKey }: ActivityPanelProps) {
  const { data, isLoading } = useQuery({
    queryKey: ['activity', boardId, refreshKey],
    queryFn: () => boardApi.activity(boardId),
  });

  return (
    <aside className="flex w-72 shrink-0 flex-col border-l border-slate-200 bg-white">
      <div className="border-b border-slate-100 px-4 py-3">
        <h3 className="text-sm font-semibold text-slate-700">Members</h3>
        <div className="mt-2 flex flex-wrap gap-2">
          {members.map((m) => (
            <div key={m.id} className="flex items-center gap-1.5" title={`${m.user.displayName} · ${m.role}`}>
              <Avatar user={m.user} size="sm" />
            </div>
          ))}
        </div>
      </div>
      <div className="flex-1 overflow-y-auto px-4 py-3">
        <h3 className="mb-2 text-sm font-semibold text-slate-700">Activity</h3>
        {isLoading && <Spinner />}
        {data && data.length === 0 && (
          <p className="text-xs text-slate-400">No activity yet.</p>
        )}
        <ul className="space-y-3">
          {data?.map((a) => (
            <li key={a.id} className="flex gap-2 text-sm">
              <Avatar user={a.actor} size="sm" />
              <div className="leading-tight">
                <p className="text-slate-700">
                  <span className="font-semibold">{a.actor.displayName}</span> {a.message}
                </p>
                <p className="text-xs text-slate-400">{timeAgo(a.createdAt)}</p>
              </div>
            </li>
          ))}
        </ul>
      </div>
    </aside>
  );
}
