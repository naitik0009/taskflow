import { FormEvent, useEffect, useState } from 'react';
import Modal from './Modal';
import type { BoardMember, Card } from '../types';
import type { CardInput } from '../api/endpoints';

interface CardModalProps {
  card: Card | null;
  members: BoardMember[];
  readOnly: boolean;
  saving: boolean;
  onClose: () => void;
  onSave: (cardId: string, input: CardInput) => void;
  onDelete: (cardId: string) => void;
}

function toDateInput(value?: string | null): string {
  if (!value) return '';
  return new Date(value).toISOString().slice(0, 10);
}

export default function CardModal({
  card,
  members,
  readOnly,
  saving,
  onClose,
  onSave,
  onDelete,
}: CardModalProps) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [assigneeId, setAssigneeId] = useState('');
  const [labels, setLabels] = useState('');
  const [dueDate, setDueDate] = useState('');

  useEffect(() => {
    if (card) {
      setTitle(card.title);
      setDescription(card.description ?? '');
      setAssigneeId(card.assignee?.id ?? '');
      setLabels(card.labels ?? '');
      setDueDate(toDateInput(card.dueDate));
    }
  }, [card]);

  if (!card) return null;

  const submit = (e: FormEvent) => {
    e.preventDefault();
    onSave(card.id, {
      title: title.trim(),
      description: description.trim() || null,
      assigneeId: assigneeId || null,
      labels: labels.trim() || null,
      dueDate: dueDate ? new Date(dueDate).toISOString() : null,
    });
  };

  return (
    <Modal open={!!card} title={readOnly ? 'Card details' : 'Edit card'} onClose={onClose}>
      <form onSubmit={submit} className="space-y-4">
        <label className="block">
          <span className="mb-1 block text-sm font-medium text-slate-600">Title</span>
          <input
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            disabled={readOnly}
            required
            className="w-full rounded-lg border border-slate-300 px-3 py-2 outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100 disabled:bg-slate-50"
          />
        </label>
        <label className="block">
          <span className="mb-1 block text-sm font-medium text-slate-600">Description</span>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            disabled={readOnly}
            rows={4}
            className="w-full resize-none rounded-lg border border-slate-300 px-3 py-2 outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100 disabled:bg-slate-50"
          />
        </label>
        <div className="grid grid-cols-2 gap-3">
          <label className="block">
            <span className="mb-1 block text-sm font-medium text-slate-600">Assignee</span>
            <select
              value={assigneeId}
              onChange={(e) => setAssigneeId(e.target.value)}
              disabled={readOnly}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100 disabled:bg-slate-50"
            >
              <option value="">Unassigned</option>
              {members.map((m) => (
                <option key={m.user.id} value={m.user.id}>
                  {m.user.displayName}
                </option>
              ))}
            </select>
          </label>
          <label className="block">
            <span className="mb-1 block text-sm font-medium text-slate-600">Due date</span>
            <input
              type="date"
              value={dueDate}
              onChange={(e) => setDueDate(e.target.value)}
              disabled={readOnly}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100 disabled:bg-slate-50"
            />
          </label>
        </div>
        <label className="block">
          <span className="mb-1 block text-sm font-medium text-slate-600">
            Labels <span className="text-slate-400">(comma-separated)</span>
          </span>
          <input
            value={labels}
            onChange={(e) => setLabels(e.target.value)}
            disabled={readOnly}
            placeholder="feature, urgent"
            className="w-full rounded-lg border border-slate-300 px-3 py-2 outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100 disabled:bg-slate-50"
          />
        </label>

        {!readOnly && (
          <div className="flex items-center justify-between pt-2">
            <button
              type="button"
              onClick={() => onDelete(card.id)}
              className="rounded-lg px-3 py-2 text-sm font-medium text-rose-600 hover:bg-rose-50"
            >
              Delete
            </button>
            <div className="flex gap-2">
              <button
                type="button"
                onClick={onClose}
                className="rounded-lg px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={saving}
                className="rounded-lg bg-brand-600 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-700 disabled:opacity-60"
              >
                {saving ? 'Saving…' : 'Save'}
              </button>
            </div>
          </div>
        )}
      </form>
    </Modal>
  );
}
