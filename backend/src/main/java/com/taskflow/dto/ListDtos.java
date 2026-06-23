package com.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public final class ListDtos {

    private ListDtos() {
    }

    public record CreateListRequest(
            @NotBlank @Size(max = 120) String name
    ) {
    }

    public record UpdateListRequest(
            @NotBlank @Size(max = 120) String name
    ) {
    }

    public record MoveListRequest(
            UUID beforeListId,
            UUID afterListId
    ) {
    }

    public record ListDto(
            UUID id,
            String name,
            double position,
            List<CardDtos.CardDto> cards
    ) {
    }
}
