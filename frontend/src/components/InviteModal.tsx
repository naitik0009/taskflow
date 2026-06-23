import { FormEvent, useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import Modal from './Modal';
import { boardApi } from '../api/endpoints';
import { apiErrorMessage } from '../api/client';
import type { BoardRole } from '../types';

interface InviteModalProps {
  boardId: string;
  open: boolean;
  onClose: () => void;
  onInvited: () => void;
}

export default function InviteModal({ boardId, open, onClose, onInvited }: InviteModalProps) {
  const [email, setEmail] = useState('');
  const [role, setRole] = useState<BoardRole>('MEMBER');
  const [error, setError] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: () => boardApi.invite(boardId, email.trim(), role),
    onSuccess: () => {
      setEmail('');
      setError(null);
      onInvited();
      onClose();
    },
    onError: (err) => setError(apiErrorMessage(err)),
  });

  const submit = (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    mutation.mutate();
  };

  return (
    <Modal open={open} title="Invite a member" onClose={onClose}>
      <form onSubmit={submit} className="space-y-4">
        {error && (
          <div className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">{error}</div>
        )}
        <label className="block">
          <span className="mb-1 block text-sm font-medium text-slate-600">Email</span>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            autoFocus
            placeholder="teammate@example.com"
            className="w-full rounded-lg border border-slate-300 px-3 py-2 outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100"
          />
        </label>
        <label className="block">
          <span className="mb-1 block text-sm font-medium text-slate-600">Role</span>
          <select
            value={role}
            onChange={(e) => setRole(e.target.value as BoardRole)}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100"
          >
            <option value="MEMBER">Member — can edit</option>
            <option value="VIEWER">Viewer — read only</option>
          </select>
        </label>
        <div className="flex justify-end gap-2">
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={mutation.isPending}
            className="rounded-lg bg-brand-600 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-700 disabled:opacity-60"
          >
            {mutation.isPending ? 'Inviting…' : 'Send invite'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
