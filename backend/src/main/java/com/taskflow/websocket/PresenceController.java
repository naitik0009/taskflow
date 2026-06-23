package com.taskflow.websocket;

import com.taskflow.dto.BoardEvent;
import com.taskflow.dto.UserDto;
import com.taskflow.security.UserPrincipal;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.UUID;

/**
 * Lightweight presence: clients send a JOIN/LEAVE to {@code /app/boards/{id}/presence}
 * and the server rebroadcasts the actor so peers can render viewing avatars.
 */
@Controller
public class PresenceController {

    private final SimpMessagingTemplate messagingTemplate;

    public PresenceController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/boards/{boardId}/presence")
    public void presence(@DestinationVariable UUID boardId,
                         PresencePing ping,
                         Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        UserDto user = new UserDto(principal.getId(), principal.getUsername(), principal.getDisplayName());
        String type = "LEAVE".equalsIgnoreCase(ping.action()) ? "PRESENCE_LEAVE" : "PRESENCE_JOIN";
        messagingTemplate.convertAndSend("/topic/boards/" + boardId,
                BoardEvent.of(type, user));
    }

    public record PresencePing(String action) {
    }
}
