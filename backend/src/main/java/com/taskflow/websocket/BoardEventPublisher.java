package com.taskflow.websocket;

import com.taskflow.dto.BoardEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Broadcasts board mutations to all subscribers of {@code /topic/boards/{id}}. */
@Component
public class BoardEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public BoardEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(UUID boardId, BoardEvent event) {
        messagingTemplate.convertAndSend("/topic/boards/" + boardId, event);
    }
}
