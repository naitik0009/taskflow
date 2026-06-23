import type { BoardDetail, BoardEvent, Card, TaskList } from '../types';

/** Pure reducer that folds a realtime BoardEvent into the cached board state. */
export function applyBoardEvent(board: BoardDetail, event: BoardEvent): BoardDetail {
  switch (event.type) {
    case 'LIST_CREATED': {
      const list = event.payload as TaskList;
      if (board.lists.some((l) => l.id === list.id)) return board;
      return { ...board, lists: sortLists([...board.lists, { ...list, cards: list.cards ?? [] }]) };
    }
    case 'LIST_UPDATED':
    case 'LIST_MOVED': {
      const incoming = event.payload as TaskList;
      const lists = board.lists.map((l) =>
        l.id === incoming.id ? { ...l, name: incoming.name, position: incoming.position } : l,
      );
      return { ...board, lists: sortLists(lists) };
    }
    case 'LIST_DELETED': {
      const id = event.payload as string;
      return { ...board, lists: board.lists.filter((l) => l.id !== id) };
    }
    case 'CARD_CREATED':
    case 'CARD_UPDATED':
    case 'CARD_MOVED': {
      const card = event.payload as Card;
      return upsertCard(board, card);
    }
    case 'CARD_DELETED': {
      const id = event.payload as string;
      return {
        ...board,
        lists: board.lists.map((l) => ({ ...l, cards: l.cards.filter((c) => c.id !== id) })),
      };
    }
    default:
      return board;
  }
}

/** Inserts or moves a card, removing any stale copy in other lists first. */
export function upsertCard(board: BoardDetail, card: Card): BoardDetail {
  const lists = board.lists.map((list) => {
    const withoutCard = list.cards.filter((c) => c.id !== card.id);
    if (list.id === card.listId) {
      return { ...list, cards: sortCards([...withoutCard, card]) };
    }
    return { ...list, cards: withoutCard };
  });
  return { ...board, lists };
}

export function sortCards(cards: Card[]): Card[] {
  return [...cards].sort((a, b) => a.position - b.position);
}

export function sortLists(lists: TaskList[]): TaskList[] {
  return [...lists].sort((a, b) => a.position - b.position);
}
