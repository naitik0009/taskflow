import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import type { Card } from '../types';
import Avatar from './Avatar';

interface CardItemProps {
  card: Card;
  readOnly: boolean;
  onClick: () => void;
}

function labelTokens(labels?: string | null): string[] {
  return (labels ?? '')
    .split(',')
    .map((t) => t.trim())
    .filter(Boolean);
}

const LABEL_COLORS = ['bg-rose-100 text-rose-700', 'bg-amber-100 text-amber-700', 'bg-sky-100 text-sky-700', 'bg-violet-100 text-violet-700', 'bg-emerald-100 text-emerald-700'];

function colorForLabel(label: string): string {
  let hash = 0;
  for (let i = 0; i < label.length; i++) hash = label.charCodeAt(i) + ((hash << 5) - hash);
  return LABEL_COLORS[Math.abs(hash) % LABEL_COLORS.length];
}

export default function CardItem({ card, readOnly, onClick }: CardItemProps) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: card.id,
    data: { type: 'card', card },
    disabled: readOnly,
  });

  const style = {
    transform: CSS.Translate.toString(transform),
    transition,
    opacity: isDragging ? 0.4 : 1,
  };

  const labels = labelTokens(card.labels);
  const due = card.dueDate ? new Date(card.dueDate) : null;

  return (
    <div
      ref={setNodeRef}
      style={style}
      {...attributes}
      {...listeners}
      onClick={onClick}
      className={`group cursor-pointer rounded-lg border border-slate-200 bg-white p-3 shadow-sm transition hover:border-brand-300 hover:shadow ${
        readOnly ? 'cursor-default' : ''
      }`}
    >
      {labels.length > 0 && (
        <div className="mb-2 flex flex-wrap gap-1">
          {labels.map((label) => (
            <span
              key={label}
              className={`rounded px-1.5 py-0.5 text-[10px] font-semibold ${colorForLabel(label)}`}
            >
              {label}
            </span>
          ))}
        </div>
      )}
      <p className="text-sm font-medium text-slate-800">{card.title}</p>
      {(due || card.assignee) && (
        <div className="mt-2 flex items-center justify-between">
          {due ? (
            <span className="rounded bg-slate-100 px-1.5 py-0.5 text-[11px] text-slate-500">
              {due.toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}
            </span>
          ) : (
            <span />
          )}
          {card.assignee && <Avatar user={card.assignee} size="sm" />}
        </div>
      )}
    </div>
  );
}
