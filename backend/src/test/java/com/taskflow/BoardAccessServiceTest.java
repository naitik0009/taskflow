package com.taskflow;

import com.taskflow.entity.Board;
import com.taskflow.entity.BoardMember;
import com.taskflow.entity.BoardRole;
import com.taskflow.entity.User;
import com.taskflow.exception.ForbiddenException;
import com.taskflow.repository.BoardMemberRepository;
import com.taskflow.repository.BoardRepository;
import com.taskflow.service.BoardAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardAccessServiceTest {

    @Mock
    BoardRepository boardRepository;
    @Mock
    BoardMemberRepository memberRepository;
    @InjectMocks
    BoardAccessService access;

    private Board board;
    private UUID ownerId;
    private UUID memberId;
    private UUID viewerId;
    private UUID strangerId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        viewerId = UUID.randomUUID();
        strangerId = UUID.randomUUID();

        User owner = new User();
        owner.setId(ownerId);
        board = new Board();
        board.setId(UUID.randomUUID());
        board.setOwner(owner);

        lenient().when(memberRepository.findByBoardIdAndUserId(board.getId(), memberId))
                .thenReturn(Optional.of(member(memberId, BoardRole.MEMBER)));
        lenient().when(memberRepository.findByBoardIdAndUserId(board.getId(), viewerId))
                .thenReturn(Optional.of(member(viewerId, BoardRole.VIEWER)));
        lenient().when(memberRepository.findByBoardIdAndUserId(board.getId(), strangerId))
                .thenReturn(Optional.empty());
    }

    private BoardMember member(UUID userId, BoardRole role) {
        User u = new User();
        u.setId(userId);
        BoardMember m = new BoardMember();
        m.setUser(u);
        m.setRole(role);
        return m;
    }

    @Test
    void ownerResolvesToOwnerRole() {
        assertThat(access.roleFor(board, ownerId)).isEqualTo(BoardRole.OWNER);
    }

    @Test
    void viewerCanReadButCannotWrite() {
        assertThat(access.requireViewer(board, viewerId)).isEqualTo(BoardRole.VIEWER);
        assertThatThrownBy(() -> access.requireMember(board, viewerId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Viewers cannot modify");
    }

    @Test
    void memberCanWrite() {
        assertThat(access.requireMember(board, memberId)).isEqualTo(BoardRole.MEMBER);
    }

    @Test
    void ownerPassesOwnerCheck() {
        access.requireOwner(board, ownerId); // no exception
        assertThatThrownBy(() -> access.requireOwner(board, memberId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void strangerHasNoAccess() {
        assertThatThrownBy(() -> access.requireViewer(board, strangerId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("do not have access");
    }
}
