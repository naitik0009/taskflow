package com.taskflow.controller;

import com.taskflow.dto.CardDtos.CardDto;
import com.taskflow.dto.CardDtos.CreateCardRequest;
import com.taskflow.dto.CardDtos.MoveCardRequest;
import com.taskflow.dto.CardDtos.UpdateCardRequest;
import com.taskflow.security.CurrentUser;
import com.taskflow.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/boards/{boardId}")
@Tag(name = "Cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/lists/{listId}/cards")
    @Operation(summary = "Create a card in a list")
    public ResponseEntity<CardDto> create(@PathVariable UUID boardId,
                                          @PathVariable UUID listId,
                                          @Valid @RequestBody CreateCardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cardService.create(CurrentUser.id(), boardId, listId, request));
    }

    @PutMapping("/cards/{cardId}")
    @Operation(summary = "Update a card's fields")
    public CardDto update(@PathVariable UUID boardId,
                          @PathVariable UUID cardId,
                          @Valid @RequestBody UpdateCardRequest request) {
        return cardService.update(CurrentUser.id(), boardId, cardId, request);
    }

    @PatchMapping("/cards/{cardId}/move")
    @Operation(summary = "Move/reorder a card within or across lists")
    public CardDto move(@PathVariable UUID boardId,
                        @PathVariable UUID cardId,
                        @Valid @RequestBody MoveCardRequest request) {
        return cardService.move(CurrentUser.id(), boardId, cardId, request);
    }

    @DeleteMapping("/cards/{cardId}")
    @Operation(summary = "Delete a card")
    public ResponseEntity<Void> delete(@PathVariable UUID boardId,
                                       @PathVariable UUID cardId) {
        cardService.delete(CurrentUser.id(), boardId, cardId);
        return ResponseEntity.noContent().build();
    }
}
