import { FormEvent, useState } from 'react';
import { Link } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { boardApi } from '../api/endpoints';
import { apiErrorMessage } from '../api/client';
import type { BoardSummary } from '../types';
import TopBar from '../components/TopBar';
import Modal from '../components/Modal';
import Spinner from '../components/Spinner';
import Avatar from '../components/Avatar';

const ROLE_BADGE: Record<string, string> = {
  OWNER: 'bg-brand-100 text-brand-700',
  MEMBER: 'bg-emerald-100 text-emerald-700',
  VIEWER: 'bg-slate-200 text-slate-600',
};

export default function BoardsPage() {
  const queryClient = useQueryClient();
  const { data: boards, isLoading, isError, error } = useQuery({
    queryKey: ['boards'],
    queryFn: boardApi.list,
  });

  const [open, setOpen] = useState(false);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [formError, setFormError] = useState<string | null>(null);

  const createMutation = useMutation({
    mutationFn: () => boardApi.create(name.trim(), description.trim() || undefined),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['boards'] });
      setOpen(false);
      setName('');
      setDescription('');
    },
    onError: (err) => setFormError(apiErrorMessage(err)),
  });

  const onCreate = (e: FormEvent) => {
    e.preventDefault();
    setFormError(null);
    createMutation.mutate();
  };

  return (
    <div className="min-h-screen bg-slate-100">
      <TopBar />
      <main className="mx-auto max-w-6xl px-4 py-8">
        <div className="mb-6 flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-slate-800">Your boards</h1>
            <p className="text-sm text-slate-500">Organize work and collaborate in real time</p>
          </div>
          <button
            onClick={() => setOpen(true)}
            className="rounded-lg bg-brand-600 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-brand-700"
          >
            + New board
          </button>
        </div>

        {isLoading && (
          <div className="flex justify-center py-20">
            <Spinner label="Loading boards…" />
          </div>
        )}

        {isError && (
          <div className="rounded-lg bg-rose-50 px-4 py-3 text-sm text-rose-700">
            {apiErrorMessage(error, 'Failed to load boards')}
          </div>
        )}

        {boards && boards.length === 0 && (
          <div className="rounded-2xl border-2 border-dashed border-slate-300 bg-white py-16 text-center">
            <p className="text-lg font-semibold text-slate-700">No boards yet</p>
            <p className="mt-1 text-sm text-slate-500">Create your first board to get started.</p>
            <button
              onClick={() => setOpen(true)}
              className="mt-4 rounded-lg bg-brand-600 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-700"
            >
              + New board
            </button>
          </div>
        )}

        {boards && boards.length > 0 && (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {boards.map((board) => (
              <BoardCard key={board.id} board={board} />
            ))}
          </div>
        )}
      </main>

      <Modal open={open} title="Create a board" onClose={() => setOpen(false)}>
        <form onSubmit={onCreate} className="space-y-4">
          {formError && (
            <div className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">{formError}</div>
          )}
          <label className="block">
            <span className="mb-1 block text-sm font-medium text-slate-600">Name</span>
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              autoFocus
              placeholder="e.g. Product Roadmap"
              className="w-full rounded-lg border border-slate-300 px-3 py-2 outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100"
            />
          </label>
          <label className="block">
            <span className="mb-1 block text-sm font-medium text-slate-600">
              Description <span className="text-slate-400">(optional)</span>
            </span>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
              className="w-full resize-none rounded-lg border border-slate-300 px-3 py-2 outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100"
            />
          </label>
          <div className="flex justify-end gap-2">
            <button
              type="button"
              onClick={() => setOpen(false)}
              className="rounded-lg px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={createMutation.isPending}
              className="rounded-lg bg-brand-600 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-700 disabled:opacity-60"
            >
              {createMutation.isPending ? 'Creating…' : 'Create board'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}

function BoardCard({ board }: { board: BoardSummary }) {
  return (
    <Link
      to={`/boards/${board.id}`}
      className="group flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:border-brand-300 hover:shadow-md"
    >
      <div>
        <div className="flex items-start justify-between">
          <h2 className="text-lg font-semibold text-slate-800 group-hover:text-brand-700">
            {board.name}
          </h2>
          <span className={`rounded-full px-2 py-0.5 text-[11px] font-semibold ${ROLE_BADGE[board.role]}`}>
            {board.role}
          </span>
        </div>
        <p className="mt-1 line-clamp-2 text-sm text-slate-500">
          {board.description || 'No description'}
        </p>
      </div>
      <div className="mt-4 flex items-center gap-2 text-xs text-slate-400">
        <Avatar user={board.owner} size="sm" />
        <span>Owned by {board.owner.displayName}</span>
      </div>
    </Link>
  );
}
