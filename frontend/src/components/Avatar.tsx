import type { User } from '../types';

const PALETTE = [
  'bg-rose-500',
  'bg-amber-500',
  'bg-emerald-500',
  'bg-sky-500',
  'bg-violet-500',
  'bg-fuchsia-500',
];

export function initials(name: string): string {
  return name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join('');
}

function colorFor(id: string): string {
  let hash = 0;
  for (let i = 0; i < id.length; i++) {
    hash = id.charCodeAt(i) + ((hash << 5) - hash);
  }
  return PALETTE[Math.abs(hash) % PALETTE.length];
}

interface AvatarProps {
  user: User;
  size?: 'sm' | 'md';
  ring?: boolean;
}

export default function Avatar({ user, size = 'md', ring = false }: AvatarProps) {
  const dims = size === 'sm' ? 'h-7 w-7 text-[11px]' : 'h-9 w-9 text-xs';
  return (
    <span
      title={user.displayName}
      className={`inline-flex items-center justify-center rounded-full font-semibold text-white ${dims} ${colorFor(
        user.id,
      )} ${ring ? 'ring-2 ring-white' : ''}`}
    >
      {initials(user.displayName)}
    </span>
  );
}
