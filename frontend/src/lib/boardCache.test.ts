import { describe, expect, it } from 'vitest';
import { applyBoardEvent, sortCards, upsertCard } from './boardCache';
import type { BoardDetail, Card } from '../types';

const owner = { id: 'u1', email: 'o@x.dev', displayName: 'Owner' };

function makeCard(id: string, listId: string, position: number): Card {
  return {
    id,
    listId,
    title: `Card ${id}`,
    position,
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  };
}

function baseBoard(): BoardDetail {
  return {
    id: 'b1',
    name: 'Board',
    owner,
    role: 'OWNER',
    createdAt: '2026-01-01T00:00:00Z',
    members: [],
    lists: [
      { id: 'l1', name: 'To Do', position: 1024, cards: [makeCard('c1', 'l1', 1024)] },
      { id: 'l2', name: 'Done', position: 2048, cards: [] },
    ],
  };
}

describe('boardCache', () => {
  it('sorts cards by position', () => {
    const cards = [makeCard('a', 'l1', 3000), makeCard('b', 'l1', 1000)];
    expect(sortCards(cards).map((c) => c.id)).toEqual(['b', 'a']);
  });

  it('moves a card across lists, removing the stale copy', () => {
    const board = baseBoard();
    const moved = { ...makeCard('c1', 'l2', 1024), listId: 'l2' };
    const next = upsertCard(board, moved);

    expect(next.lists.find((l) => l.id === 'l1')!.cards).toHaveLength(0);
    expect(next.lists.find((l) => l.id === 'l2')!.cards.map((c) => c.id)).toEqual(['c1']);
  });

  it('applies a CARD_CREATED realtime event', () => {
    const board = baseBoard();
    const created = makeCard('c2', 'l2', 1024);
    const next = applyBoardEvent(board, { type: 'CARD_CREATED', payload: created });
    expect(next.lists.find((l) => l.id === 'l2')!.cards.map((c) => c.id)).toEqual(['c2']);
  });

  it('applies a CARD_DELETED realtime event', () => {
    const board = baseBoard();
    const next = applyBoardEvent(board, { type: 'CARD_DELETED', payload: 'c1' });
    expect(next.lists.flatMap((l) => l.cards)).toHaveLength(0);
  });

  it('ignores a duplicate LIST_CREATED', () => {
    const board = baseBoard();
    const dup = { id: 'l1', name: 'To Do', position: 1024, cards: [] };
    const next = applyBoardEvent(board, { type: 'LIST_CREATED', payload: dup });
    expect(next.lists).toHaveLength(2);
  });
});
