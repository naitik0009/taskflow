export type BoardRole = 'OWNER' | 'MEMBER' | 'VIEWER';

export interface User {
  id: string;
  email: string;
  displayName: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface Card {
  id: string;
  listId: string;
  title: string;
  description?: string | null;
  position: number;
  assignee?: User | null;
  labels?: string | null;
  dueDate?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface TaskList {
  id: string;
  name: string;
  position: number;
  cards: Card[];
}

export interface BoardMember {
  id: string;
  user: User;
  role: BoardRole;
}

export interface BoardSummary {
  id: string;
  name: string;
  description?: string | null;
  owner: User;
  role: BoardRole;
  createdAt: string;
}

export interface BoardDetail extends BoardSummary {
  members: BoardMember[];
  lists: TaskList[];
}

export interface Activity {
  id: string;
  actor: User;
  message: string;
  createdAt: string;
}

export interface BoardEvent {
  type:
    | 'LIST_CREATED'
    | 'LIST_UPDATED'
    | 'LIST_MOVED'
    | 'LIST_DELETED'
    | 'CARD_CREATED'
    | 'CARD_UPDATED'
    | 'CARD_MOVED'
    | 'CARD_DELETED'
    | 'PRESENCE_JOIN'
    | 'PRESENCE_LEAVE';
  payload: unknown;
}
