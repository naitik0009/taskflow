package com.taskflow.dto;

/**
 * Envelope broadcast over STOMP to subscribers of a board topic.
 * {@code type} drives client-side cache updates; {@code payload} carries the affected DTO.
 */
public record BoardEvent(
        String type,
        Object payload
) {
    public static BoardEvent of(String type, Object payload) {
        return new BoardEvent(type, payload);
    }
}
