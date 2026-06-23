import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { TOKEN_KEY } from '../api/client';
import type { BoardEvent, User } from '../types';

interface UseBoardSocketOptions {
  boardId: string;
  currentUser: User | null;
  onEvent: (event: BoardEvent) => void;
}

/**
 * Opens an authenticated STOMP connection over SockJS, subscribes to the board
 * topic, announces presence on connect, and cleans up on unmount. Returns the
 * set of peers currently viewing the board for the presence indicator.
 */
export function useBoardSocket({ boardId, currentUser, onEvent }: UseBoardSocketOptions) {
  const [connected, setConnected] = useState(false);
  const [viewers, setViewers] = useState<User[]>([]);
  const onEventRef = useRef(onEvent);
  onEventRef.current = onEvent;

  useEffect(() => {
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token || !currentUser) return;

    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 4000,
      onConnect: () => {
        setConnected(true);
        client.subscribe(`/topic/boards/${boardId}`, (message) => {
          const event = JSON.parse(message.body) as BoardEvent;
          if (event.type === 'PRESENCE_JOIN') {
            const peer = event.payload as User;
            setViewers((prev) =>
              prev.some((v) => v.id === peer.id) ? prev : [...prev, peer],
            );
            // Reply so the newcomer learns we are here too.
            if (peer.id !== currentUser.id) {
              announce(client, boardId, 'JOIN');
            }
            return;
          }
          if (event.type === 'PRESENCE_LEAVE') {
            const peer = event.payload as User;
            setViewers((prev) => prev.filter((v) => v.id !== peer.id));
            return;
          }
          onEventRef.current(event);
        });
        announce(client, boardId, 'JOIN');
      },
      onDisconnect: () => setConnected(false),
      onStompError: () => setConnected(false),
    });

    client.activate();

    return () => {
      if (client.connected) {
        announce(client, boardId, 'LEAVE');
      }
      void client.deactivate();
      setConnected(false);
      setViewers([]);
    };
  }, [boardId, currentUser]);

  return { connected, viewers };
}

function announce(client: Client, boardId: string, action: 'JOIN' | 'LEAVE') {
  if (!client.connected) return;
  client.publish({
    destination: `/app/boards/${boardId}/presence`,
    body: JSON.stringify({ action }),
  });
}
