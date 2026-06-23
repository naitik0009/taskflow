package com.taskflow.service;

import com.taskflow.entity.Board;
import com.taskflow.entity.BoardMember;
import com.taskflow.entity.BoardRole;
import com.taskflow.exception.ForbiddenException;
import com.taskflow.exception.NotFoundException;
import com.taskflow.repository.BoardMemberRepository;
import com.taskflow.repository.BoardRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Central RBAC gate. Resolves the effective role of a user on a board and
 * enforces the minimum role required for an operation. Owners always rank
 * highest; explicit membership covers MEMBER/VIEWER.
 */
@Service
public class BoardAccessService {

    private final BoardRepository boardRepository;
    private final BoardMemberRepository memberRepository;

    public BoardAccessService(BoardRepository boardRepository,
                              BoardMemberRepository memberRepository) {
        this.boardRepository = boardRepository;
        this.memberRepository = memberRepository;
    }

    public Board getBoardOrThrow(UUID boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("Board not found"));
    }

    public BoardRole roleFor(Board board, UUID userId) {
        if (board.getOwner().getId().equals(userId)) {
            return BoardRole.OWNER;
        }
        return memberRepository.findByBoardIdAndUserId(board.getId(), userId)
                .map(BoardMember::getRole)
                .orElseThrow(() -> new ForbiddenException("You do not have access to this board"));
    }

    /** Read access: any role (OWNER, MEMBER, VIEWER). */
    public BoardRole requireViewer(Board board, UUID userId) {
        return roleFor(board, userId);
    }

    /** Write access: OWNER or MEMBER. VIEWER is rejected. */
    public BoardRole requireMember(Board board, UUID userId) {
        BoardRole role = roleFor(board, userId);
        if (role == BoardRole.VIEWER) {
            throw new ForbiddenException("Viewers cannot modify this board");
        }
        return role;
    }

    /** Administrative access: OWNER only (board settings, membership). */
    public void requireOwner(Board board, UUID userId) {
        if (!board.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only the board owner can perform this action");
        }
    }
}
