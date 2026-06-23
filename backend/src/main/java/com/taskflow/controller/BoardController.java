package com.taskflow.controller;

import com.taskflow.dto.ActivityDto;
import com.taskflow.dto.BoardDtos.BoardDetail;
import com.taskflow.dto.BoardDtos.BoardSummary;
import com.taskflow.dto.BoardDtos.CreateBoardRequest;
import com.taskflow.dto.BoardDtos.InviteMemberRequest;
import com.taskflow.dto.BoardDtos.MemberDto;
import com.taskflow.dto.BoardDtos.UpdateBoardRequest;
import com.taskflow.dto.BoardDtos.UpdateMemberRoleRequest;
import com.taskflow.security.CurrentUser;
import com.taskflow.service.ActivityService;
import com.taskflow.service.BoardAccessService;
import com.taskflow.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/boards")
@Tag(name = "Boards")
public class BoardController {

    private final BoardService boardService;
    private final ActivityService activityService;
    private final BoardAccessService access;

    public BoardController(BoardService boardService,
                          ActivityService activityService,
                          BoardAccessService access) {
        this.boardService = boardService;
        this.activityService = activityService;
        this.access = access;
    }

    @GetMapping
    @Operation(summary = "List boards the current user owns or is a member of")
    public List<BoardSummary> list() {
        return boardService.listForUser(CurrentUser.id());
    }

    @PostMapping
    @Operation(summary = "Create a board")
    public ResponseEntity<BoardSummary> create(@Valid @RequestBody CreateBoardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(boardService.create(CurrentUser.id(), request));
    }

    @GetMapping("/{boardId}")
    @Operation(summary = "Get a board with lists, cards and members")
    public BoardDetail get(@PathVariable UUID boardId) {
        return boardService.getDetail(CurrentUser.id(), boardId);
    }

    @PutMapping("/{boardId}")
    @Operation(summary = "Update board name/description")
    public BoardSummary update(@PathVariable UUID boardId,
                               @Valid @RequestBody UpdateBoardRequest request) {
        return boardService.update(CurrentUser.id(), boardId, request);
    }

    @DeleteMapping("/{boardId}")
    @Operation(summary = "Delete a board (owner only)")
    public ResponseEntity<Void> delete(@PathVariable UUID boardId) {
        boardService.delete(CurrentUser.id(), boardId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{boardId}/members")
    @Operation(summary = "Invite a user to the board (owner only)")
    public ResponseEntity<MemberDto> invite(@PathVariable UUID boardId,
                                            @Valid @RequestBody InviteMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(boardService.invite(CurrentUser.id(), boardId, request));
    }

    @PutMapping("/{boardId}/members/{memberId}")
    @Operation(summary = "Change a member's role (owner only)")
    public MemberDto updateMemberRole(@PathVariable UUID boardId,
                                      @PathVariable UUID memberId,
                                      @Valid @RequestBody UpdateMemberRoleRequest request) {
        return boardService.updateMemberRole(CurrentUser.id(), boardId, memberId, request);
    }

    @DeleteMapping("/{boardId}/members/{memberId}")
    @Operation(summary = "Remove a member (owner only)")
    public ResponseEntity<Void> removeMember(@PathVariable UUID boardId,
                                             @PathVariable UUID memberId) {
        boardService.removeMember(CurrentUser.id(), boardId, memberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{boardId}/activity")
    @Operation(summary = "Recent activity feed for a board")
    public List<ActivityDto> activity(@PathVariable UUID boardId,
                                      @RequestParam(defaultValue = "30") int limit) {
        // Read access check.
        access.requireViewer(access.getBoardOrThrow(boardId), CurrentUser.id());
        return activityService.recent(boardId, Math.min(limit, 100));
    }
}
