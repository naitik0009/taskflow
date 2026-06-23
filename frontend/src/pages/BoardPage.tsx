import { useCallback, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  DndContext,
  DragEndEvent,
  DragOverlay,
  DragStartEvent,
  PointerSensor,
  closestCorners,
  useSensor,
  useSensors,
} from '@dnd-kit/core';
import { boardApi, cardApi, listApi } from '../api/endpoints';
import type { CardInput } from '../api/endpoints';
import { apiErrorMessage } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { useBoardSocket } from '../hooks/useBoardSocket';
import { applyBoardEvent, upsertCard } from '../lib/boardCache';
import type { BoardDetail, BoardEvent, Card } from '../types';
import TopBar from '../components/TopBar';
import Column from '../components/Column';
import CardItem from '../components/CardItem';
import CardModal from '../components/CardModal';
import PresenceBar from '../components/PresenceBar';
import ActivityPanel from '../components/ActivityPanel';
import InviteModal from '../components/InviteModal';
import Spinner from '../components/Spinner';

export default function BoardPage() {
  const { boardId = '' } = useParams();
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const queryKey = useMemo(() => ['board', boardId], [boardId]);

  const { data: board, isLoading, isError, error } = useQuery({
    queryKey,
    queryFn: () => boardApi.get(boardId),
  });

  const [activeCard, setActiveCard] = useState<Card | null>(null);
  const [selectedCard, setSelectedCard] = useState<Card | null>(null);
  const [inviteOpen, setInviteOpen] = useState(false);
  const [activityKey, setActivityKey] = useState(0);
  const [addListOpen, setAddListOpen] = useState(false);
  const [newListName, setNewListName] = useState('');

  const readOnly = board?.role === 'VIEWER';

  const setBoard = useCallback(
    (updater: (b: BoardDetail) => BoardDetail) => {
      queryClient.setQueryData<BoardDetail>(queryKey, (prev) => (prev ? updater(prev) : prev));
    },
    [queryClient, queryKey],
  );

  const bumpActivity = useCallback(() => setActivityKey((k) => k + 1), []);

  // Realtime: fold incoming events into the cached board.
  const handleEvent = useCallback(
    (event: BoardEvent) => {
      setBoard((b) => applyBoardEvent(b, event));
      bumpActivity();
    },
    [setBoard, bumpActivity],
  );

  const { connected, viewers } = useBoardSocket({
    boardId,
    currentUser: user,
    onEvent: handleEvent,
  });

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 6 } }),
  );

  const findCard = (id: string): Card | undefined =>
    board?.lists.flatMap((l) => l.cards).find((c) => c.id === id);

  const findListIdByCard = (cardId: string): string | undefined =>
    board?.lists.find((l) => l.cards.some((c) => c.id === cardId))?.id;

  const moveMutation = useMutation({
    mutationFn: (vars: {
      cardId: string;
      targetListId: string;
      beforeCardId: string | null;
      afterCardId: string | null;
    }) =>
      cardApi.move(boardId, vars.cardId, vars.targetListId, vars.beforeCardId, vars.afterCardId),
    onSuccess: (card) => {
      setBoard((b) => upsertCard(b, card));
      bumpActivity();
    },
    onError: () => {
      // Reconcile with server truth if the optimistic move failed.
      queryClient.invalidateQueries({ queryKey });
    },
  });

  const onDragStart = (e: DragStartEvent) => {
    const card = findCard(String(e.active.id));
    if (card) setActiveCard(card);
  };

  const onDragEnd = (e: DragEndEvent) => {
    setActiveCard(null);
    const { active, over } = e;
    if (!over || !board) return;

    const cardId = String(active.id);
    const sourceListId = findListIdByCard(cardId);
    if (!sourceListId) return;

    // Determine the target list and the index to drop at.
    let targetListId: string;
    let overCardId: string | null = null;

    if (over.data.current?.type === 'list') {
      targetListId = over.data.current.listId as string;
    } else {
      overCardId = String(over.id);
      targetListId = findListIdByCard(overCardId) ?? sourceListId;
    }

    const targetList = board.lists.find((l) => l.id === targetListId);
    if (!targetList) return;

    // Build the target ordering without the dragged card.
    const siblings = targetList.cards.filter((c) => c.id !== cardId);
    let insertIndex = siblings.length;
    if (overCardId) {
      const idx = siblings.findIndex((c) => c.id === overCardId);
      if (idx !== -1) insertIndex = idx;
    }

    const beforeCardId = insertIndex > 0 ? siblings[insertIndex - 1].id : null;
    const afterCardId = insertIndex < siblings.length ? siblings[insertIndex].id : null;

    if (sourceListId === targetListId && !overCardId) {
      return; // dropped on its own list with no positional change
    }

    // Optimistic reorder.
    const dragged = findCard(cardId);
    if (dragged) {
      const optimistic: Card = { ...dragged, listId: targetListId };
      setBoard((b) => upsertCard(b, optimistic));
    }

    moveMutation.mutate({ cardId, targetListId, beforeCardId, afterCardId });
  };

  const addCardMutation = useMutation({
    mutationFn: (vars: { listId: string; title: string }) =>
      cardApi.create(boardId, vars.listId, { title: vars.title }),
    onSuccess: (card) => {
      setBoard((b) => upsertCard(b, card));
      bumpActivity();
    },
  });

  const updateCardMutation = useMutation({
    mutationFn: (vars: { cardId: string; input: CardInput }) =>
      cardApi.update(boardId, vars.cardId, vars.input),
    onSuccess: (card) => {
      setBoard((b) => upsertCard(b, card));
      setSelectedCard(null);
    },
  });

  const deleteCardMutation = useMutation({
    mutationFn: (cardId: string) => cardApi.remove(boardId, cardId),
    onSuccess: (_data, cardId) => {
      setBoard((b) => ({
        ...b,
        lists: b.lists.map((l) => ({ ...l, cards: l.cards.filter((c) => c.id !== cardId) })),
      }));
      setSelectedCard(null);
      bumpActivity();
    },
  });

  const addListMutation = useMutation({
    mutationFn: (name: string) => listApi.create(boardId, name),
    onSuccess: (list) => {
      setBoard((b) => ({ ...b, lists: [...b.lists, { ...list, cards: list.cards ?? [] }] }));
      setNewListName('');
      setAddListOpen(false);
      bumpActivity();
    },
  });

  const deleteListMutation = useMutation({
    mutationFn: (listId: string) => listApi.remove(boardId, listId),
    onSuccess: (_data, listId) => {
      setBoard((b) => ({ ...b, lists: b.lists.filter((l) => l.id !== listId) }));
      bumpActivity();
    },
  });

  if (isLoading) {
    return (
      <div className="flex h-screen flex-col">
        <TopBar />
        <div className="flex flex-1 items-center justify-center">
          <Spinner label="Loading board…" />
        </div>
      </div>
    );
  }

  if (isError || !board) {
    return (
      <div className="flex h-screen flex-col">
        <TopBar />
        <div className="flex flex-1 flex-col items-center justify-center gap-3">
          <p className="text-rose-600">{apiErrorMessage(error, 'Unable to load board')}</p>
          <Link to="/" className="text-sm font-semibold text-brand-600 hover:underline">
            Back to boards
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="flex h-screen flex-col overflow-hidden">
      <TopBar>
        <span className="text-slate-300">/</span>
        <span className="text-sm font-semibold text-slate-700">{board.name}</span>
      </TopBar>

      <div className="flex items-center justify-between border-b border-slate-200 bg-white px-4 py-2">
        <div className="flex items-center gap-3">
          {readOnly && (
            <span className="rounded-full bg-slate-200 px-2 py-0.5 text-xs font-semibold text-slate-600">
              Read only
            </span>
          )}
          <PresenceBar connected={connected} viewers={viewers} />
        </div>
        {board.role === 'OWNER' && (
          <button
            onClick={() => setInviteOpen(true)}
            className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-600 hover:bg-slate-50"
          >
            + Invite
          </button>
        )}
      </div>

      <div className="flex flex-1 overflow-hidden">
        <div className="thin-scrollbar flex-1 overflow-x-auto p-4">
          <DndContext
            sensors={sensors}
            collisionDetection={closestCorners}
            onDragStart={onDragStart}
            onDragEnd={onDragEnd}
          >
            <div className="flex h-full items-start gap-4">
              {board.lists.map((list) => (
                <Column
                  key={list.id}
                  list={list}
                  readOnly={readOnly ?? false}
                  onAddCard={(listId, title) => addCardMutation.mutate({ listId, title })}
                  onOpenCard={setSelectedCard}
                  onDeleteList={(listId) => deleteListMutation.mutate(listId)}
                />
              ))}

              {!readOnly && (
                <div className="w-72 shrink-0">
                  {addListOpen ? (
                    <div className="rounded-xl bg-slate-200/70 p-2">
                      <input
                        value={newListName}
                        autoFocus
                        onChange={(e) => setNewListName(e.target.value)}
                        onKeyDown={(e) => {
                          if (e.key === 'Enter' && newListName.trim()) {
                            addListMutation.mutate(newListName.trim());
                          }
                          if (e.key === 'Escape') setAddListOpen(false);
                        }}
                        placeholder="List name…"
                        className="w-full rounded-lg border border-slate-300 px-2 py-1.5 text-sm outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100"
                      />
                      <div className="mt-2 flex gap-2">
                        <button
                          onClick={() => newListName.trim() && addListMutation.mutate(newListName.trim())}
                          className="rounded-lg bg-brand-600 px-3 py-1 text-sm font-semibold text-white hover:bg-brand-700"
                        >
                          Add list
                        </button>
                        <button
                          onClick={() => setAddListOpen(false)}
                          className="rounded-lg px-2 py-1 text-sm text-slate-500 hover:bg-slate-300"
                        >
                          Cancel
                        </button>
                      </div>
                    </div>
                  ) : (
                    <button
                      onClick={() => setAddListOpen(true)}
                      className="w-full rounded-xl border-2 border-dashed border-slate-300 px-3 py-2.5 text-sm font-medium text-slate-500 hover:border-brand-300 hover:bg-white"
                    >
                      + Add a list
                    </button>
                  )}
                </div>
              )}
            </div>

            <DragOverlay>
              {activeCard ? (
                <div className="w-72 rotate-2">
                  <CardItem card={activeCard} readOnly onClick={() => {}} />
                </div>
              ) : null}
            </DragOverlay>
          </DndContext>
        </div>

        <ActivityPanel boardId={boardId} members={board.members} refreshKey={activityKey} />
      </div>

      <CardModal
        card={selectedCard}
        members={board.members}
        readOnly={readOnly ?? false}
        saving={updateCardMutation.isPending}
        onClose={() => setSelectedCard(null)}
        onSave={(cardId, input) => updateCardMutation.mutate({ cardId, input })}
        onDelete={(cardId) => deleteCardMutation.mutate(cardId)}
      />

      <InviteModal
        boardId={boardId}
        open={inviteOpen}
        onClose={() => setInviteOpen(false)}
        onInvited={() => queryClient.invalidateQueries({ queryKey })}
      />
    </div>
  );
}
