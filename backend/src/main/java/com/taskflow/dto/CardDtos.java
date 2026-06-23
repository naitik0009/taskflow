package com.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class CardDtos {

    private CardDtos() {
    }

    public record CreateCardRequest(
            @NotBlank @Size(max = 250) String title,
            @Size(max = 4000) String description,
            UUID assigneeId,
            String labels,
            Instant dueDate
    ) {
    }

    public record UpdateCardRequest(
            @NotBlank @Size(max = 250) String title,
            @Size(max = 4000) String description,
            UUID assigneeId,
            String labels,
            Instant dueDate
    ) {
    }

    /**
     * Move a card to a target list at a position between two sibling cards.
     * Either neighbour may be null (top/bottom of the list).
     */
    public record MoveCardRequest(
            @NotNull UUID targetListId,
            UUID beforeCardId,
            UUID afterCardId
    ) {
    }

    public record CardDto(
            UUID id,
            UUID listId,
            String title,
            String description,
            double position,
            UserDto assignee,
            String labels,
            Instant dueDate,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
