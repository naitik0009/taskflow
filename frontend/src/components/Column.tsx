import { useState } from 'react';
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { useDroppable } from '@dnd-kit/core';
import type { Card, TaskList } from '../types';
import CardItem from './CardItem';

interface ColumnProps {
  list: TaskList;
  readOnly: boolean;
  onAddCard: (listId: string, title: string) => void;
  onOpenCard: (card: Card) => void;
  onDeleteList: (listId: string) => void;
}

export default function Column({ list, readOnly, onAddCard, onOpenCard, onDeleteList }: ColumnProps) {
  const { setNodeRef, isOver } = useDroppable({
    id: `list:${list.id}`,
    data: { type: 'list', listId: list.id },
  });
  const [adding, setAdding] = useState(false);
  const [title, setTitle] = useState('');

  const submit = () => {
    const value = title.trim();
    if (value) {
      onAddCard(list.id, value);
      setTitle('');
    }
    setAdding(false);
  };

  return (
    <div className="flex h-full w-72 shrink-0 flex-col rounded-xl bg-slate-200/70">
      <div className="flex items-center justify-between px-3 py-2.5">
        <h3 className="text-sm font-semibold text-slate-700">
          {list.name}
          <span className="ml-2 rounded-full bg-slate-300/70 px-1.5 text-xs font-medium text-slate-600">
            {list.cards.length}
          </span>
        </h3>
        {!readOnly && (
          <button
            onClick={() => onDeleteList(list.id)}
            aria-label="Delete list"
            className="rounded p-1 text-slate-400 hover:bg-slate-300 hover:text-slate-600"
          >
            ✕
          </button>
        )}
      </div>

      <div
        ref={setNodeRef}
        className={`thin-scrollbar flex-1 space-y-2 overflow-y-auto px-2 pb-2 ${
          isOver ? 'rounded-lg bg-brand-100/50' : ''
        }`}
      >
        <SortableContext
          items={list.cards.map((c) => c.id)}
          strategy={verticalListSortingStrategy}
        >
          {list.cards.map((card) => (
            <CardItem
              key={card.id}
              card={card}
              readOnly={readOnly}
              onClick={() => onOpenCard(card)}
            />
          ))}
        </SortableContext>

        {list.cards.length === 0 && !adding && (
          <p className="px-1 py-3 text-center text-xs text-slate-400">No cards yet</p>
        )}
      </div>

      {!readOnly && (
        <div className="p-2">
          {adding ? (
            <div className="space-y-2">
              <textarea
                value={title}
                autoFocus
                onChange={(e) => setTitle(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    submit();
                  }
                  if (e.key === 'Escape') setAdding(false);
                }}
                rows={2}
                placeholder="Enter a title…"
                className="w-full resize-none rounded-lg border border-slate-300 px-2 py-1.5 text-sm outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100"
              />
              <div className="flex gap-2">
                <button
                  onClick={submit}
                  className="rounded-lg bg-brand-600 px-3 py-1 text-sm font-semibold text-white hover:bg-brand-700"
                >
                  Add
                </button>
                <button
                  onClick={() => setAdding(false)}
                  className="rounded-lg px-2 py-1 text-sm text-slate-500 hover:bg-slate-300"
                >
                  Cancel
                </button>
              </div>
            </div>
          ) : (
            <button
              onClick={() => setAdding(true)}
              className="w-full rounded-lg px-2 py-1.5 text-left text-sm font-medium text-slate-500 hover:bg-slate-300/70"
            >
              + Add a card
            </button>
          )}
        </div>
      )}
    </div>
  );
}
