import { api } from './client';
import type {
  Activity,
  AuthResponse,
  BoardDetail,
  BoardMember,
  BoardRole,
  BoardSummary,
  Card,
  TaskList,
  User,
} from '../types';

export const authApi = {
  register: (email: string, displayName: string, password: string) =>
    api.post<AuthResponse>('/auth/register', { email, displayName, password }).then((r) => r.data),
  login: (email: string, password: string) =>
    api.post<AuthResponse>('/auth/login', { email, password }).then((r) => r.data),
  me: () => api.get<User>('/auth/me').then((r) => r.data),
};

export const boardApi = {
  list: () => api.get<BoardSummary[]>('/boards').then((r) => r.data),
  get: (id: string) => api.get<BoardDetail>(`/boards/${id}`).then((r) => r.data),
  create: (name: string, description?: string) =>
    api.post<BoardSummary>('/boards', { name, description }).then((r) => r.data),
  remove: (id: string) => api.delete(`/boards/${id}`).then(() => undefined),
  invite: (boardId: string, email: string, role: BoardRole) =>
    api
      .post<BoardMember>(`/boards/${boardId}/members`, { email, role })
      .then((r) => r.data),
  activity: (boardId: string) =>
    api.get<Activity[]>(`/boards/${boardId}/activity`).then((r) => r.data),
};

export const listApi = {
  create: (boardId: string, name: string) =>
    api.post<TaskList>(`/boards/${boardId}/lists`, { name }).then((r) => r.data),
  rename: (boardId: string, listId: string, name: string) =>
    api.put<TaskList>(`/boards/${boardId}/lists/${listId}`, { name }).then((r) => r.data),
  remove: (boardId: string, listId: string) =>
    api.delete(`/boards/${boardId}/lists/${listId}`).then(() => undefined),
};

export interface CardInput {
  title: string;
  description?: string | null;
  assigneeId?: string | null;
  labels?: string | null;
  dueDate?: string | null;
}

export const cardApi = {
  create: (boardId: string, listId: string, input: CardInput) =>
    api
      .post<Card>(`/boards/${boardId}/lists/${listId}/cards`, input)
      .then((r) => r.data),
  update: (boardId: string, cardId: string, input: CardInput) =>
    api.put<Card>(`/boards/${boardId}/cards/${cardId}`, input).then((r) => r.data),
  move: (
    boardId: string,
    cardId: string,
    targetListId: string,
    beforeCardId: string | null,
    afterCardId: string | null,
  ) =>
    api
      .patch<Card>(`/boards/${boardId}/cards/${cardId}/move`, {
        targetListId,
        beforeCardId,
        afterCardId,
      })
      .then((r) => r.data),
  remove: (boardId: string, cardId: string) =>
    api.delete(`/boards/${boardId}/cards/${cardId}`).then(() => undefined),
};
