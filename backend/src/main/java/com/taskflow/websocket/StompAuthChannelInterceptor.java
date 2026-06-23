package com.taskflow.websocket;

import com.taskflow.security.AppUserDetailsService;
import com.taskflow.security.JwtService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Validates the JWT presented on the STOMP CONNECT frame and binds the
 * authenticated principal to the WebSocket session so downstream messages
 * carry identity.
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final AppUserDetailsService userDetailsService;

    public StompAuthChannelInterceptor(JwtService jwtService,
                                       AppUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Missing or invalid Authorization header");
            }
            String token = authHeader.substring(7);
            if (!jwtService.isValid(token)) {
                throw new IllegalArgumentException("Invalid JWT on STOMP connect");
            }
            UUID userId = jwtService.extractUserId(token);
            UserDetails details = userDetailsService.loadByUserId(userId);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
            accessor.setUser(auth);
        }
        return message;
    }
}
