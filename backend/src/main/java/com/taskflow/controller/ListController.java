package com.taskflow.controller;

import com.taskflow.dto.ListDtos.CreateListRequest;
import com.taskflow.dto.ListDtos.ListDto;
import com.taskflow.dto.ListDtos.MoveListRequest;
import com.taskflow.dto.ListDtos.UpdateListRequest;
import com.taskflow.security.CurrentUser;
import com.taskflow.service.ListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/boards/{boardId}/lists")
@Tag(name = "Lists")
public class ListController {

    private final ListService listService;

    public ListController(ListService listService) {
        this.listService = listService;
    }

    @PostMapping
    @Operation(summary = "Create a list (column) on a board")
    public ResponseEntity<ListDto> create(@PathVariable UUID boardId,
                                          @Valid @RequestBody CreateListRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(listService.create(CurrentUser.id(), boardId, request));
    }

    @PutMapping("/{listId}")
    @Operation(summary = "Rename a list")
    public ListDto rename(@PathVariable UUID boardId,
                          @PathVariable UUID listId,
                          @Valid @RequestBody UpdateListRequest request) {
        return listService.rename(CurrentUser.id(), boardId, listId, request);
    }

    @PatchMapping("/{listId}/move")
    @Operation(summary = "Reorder a list between two siblings")
    public ListDto move(@PathVariable UUID boardId,
                        @PathVariable UUID listId,
                        @Valid @RequestBody MoveListRequest request) {
        return listService.move(CurrentUser.id(), boardId, listId, request);
    }

    @DeleteMapping("/{listId}")
    @Operation(summary = "Delete a list and its cards")
    public ResponseEntity<Void> delete(@PathVariable UUID boardId,
                                       @PathVariable UUID listId) {
        listService.delete(CurrentUser.id(), boardId, listId);
        return ResponseEntity.noContent().build();
    }
}
