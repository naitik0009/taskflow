package com.taskflow.service;

import com.taskflow.dto.BoardDtos.BoardDetail;
import com.taskflow.dto.BoardDtos.BoardSummary;
import com.taskflow.dto.BoardDtos.CreateBoardRequest;
import com.taskflow.dto.BoardDtos.InviteMemberRequest;
import com.taskflow.dto.BoardDtos.MemberDto;
import com.taskflow.dto.BoardDtos.UpdateBoardRequest;
import com.taskflow.dto.BoardDtos.UpdateMemberRoleRequest;
import com.taskflow.dto.ListDtos.ListDto;
import com.taskflow.entity.Board;
import com.taskflow.entity.BoardMember;
import com.taskflow.entity.BoardRole;
import com.taskflow.entity.Card;
import com.taskflow.entity.TaskList;
import com.taskflow.entity.User;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.NotFoundException;
import com.taskflow.mapper.DtoMapper;
import com.taskflow.repository.BoardMemberRepository;
import com.taskflow.repository.BoardRepository;
import com.taskflow.repository.CardRepository;
import com.taskflow.repository.TaskListRepository;
import com.taskflow.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardMemberRepository memberRepository;
    private final TaskListRepository listRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final BoardAccessService access;
    private final ActivityService activityService;
    private final DtoMapper mapper;

    public BoardService(BoardRepository boardRepository,
                        BoardMemberRepository memberRepository,
                        TaskListRepository listRepository,
                        CardRepository cardRepository,
                        UserRepository userRepository,
                        BoardAccessService access,
                        ActivityService activityService,
                        DtoMapper mapper) {
        this.boardRepository = boardRepository;
        this.memberRepository = memberRepository;
        this.listRepository = listRepository;
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.access = access;
        this.activityService = activityService;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<BoardSummary> listForUser(UUID userId) {
        return boardRepository.findAllForUser(userId).stream()
                .map(board -> new BoardSummary(
                        board.getId(),
                        board.getName(),
                        board.getDescription(),
                        mapper.toUserDto(board.getOwner()),
                        access.roleFor(board, userId),
                        board.getCreatedAt()))
                .toList();
    }

    @Transactional
    public BoardSummary create(UUID userId, CreateBoardRequest request) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Board board = Board.builder()
                .name(request.name().trim())
                .description(request.description())
                .owner(owner)
                .build();
        board = boardRepository.save(board);
        activityService.record(board, userId, "created the board");
        return new BoardSummary(
                board.getId(),
                board.getName(),
                board.getDescription(),
                mapper.toUserDto(owner),
                BoardRole.OWNER,
                board.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public BoardDetail getDetail(UUID userId, UUID boardId) {
        Board board = access.getBoardOrThrow(boardId);
        BoardRole role = access.requireViewer(board, userId);

        List<TaskList> lists = listRepository.findByBoardIdOrderByPositionAsc(boardId);
        Map<UUID, List<Card>> cardsByList = cardRepository
                .findByListBoardIdOrderByPositionAsc(boardId).stream()
                .collect(Collectors.groupingBy(c -> c.getList().getId()));

        List<ListDto> listDtos = lists.stream()
                .map(list -> mapper.toListDto(list, cardsByList.getOrDefault(list.getId(), List.of())))
                .toList();

        List<MemberDto> members = memberRepository.findByBoardId(boardId).stream()
                .map(mapper::toMemberDto)
                .toList();

        return new BoardDetail(
                board.getId(),
                board.getName(),
                board.getDescription(),
                mapper.toUserDto(board.getOwner()),
                role,
                members,
                listDtos,
                board.getCreatedAt());
    }

    @Transactional
    public BoardSummary update(UUID userId, UUID boardId, UpdateBoardRequest request) {
        Board board = access.getBoardOrThrow(boardId);
        access.requireMember(board, userId);
        board.setName(request.name().trim());
        board.setDescription(request.description());
        board = boardRepository.save(board);
        return new BoardSummary(
                board.getId(),
                board.getName(),
                board.getDescription(),
                mapper.toUserDto(board.getOwner()),
                access.roleFor(board, userId),
                board.getCreatedAt());
    }

    @Transactional
    public void delete(UUID userId, UUID boardId) {
        Board board = access.getBoardOrThrow(boardId);
        access.requireOwner(board, userId);
        boardRepository.delete(board);
    }

    @Transactional
    public MemberDto invite(UUID userId, UUID boardId, InviteMemberRequest request) {
        Board board = access.getBoardOrThrow(boardId);
        access.requireOwner(board, userId);

        String email = request.email().toLowerCase().trim();
        User invitee = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("No user with that email"));

        if (board.getOwner().getId().equals(invitee.getId())) {
            throw new BadRequestException("The owner is already a member");
        }
        if (memberRepository.existsByBoardIdAndUserId(boardId, invitee.getId())) {
            throw new BadRequestException("User is already a member of this board");
        }
        if (request.role() == BoardRole.OWNER) {
            throw new BadRequestException("Cannot assign OWNER role");
        }

        BoardMember member = BoardMember.builder()
                .board(board)
                .user(invitee)
                .role(request.role())
                .build();
        member = memberRepository.save(member);
        activityService.record(board, userId, "invited " + invitee.getDisplayName());
        return mapper.toMemberDto(member);
    }

    @Transactional
    public MemberDto updateMemberRole(UUID userId, UUID boardId, UUID memberId,
                                      UpdateMemberRoleRequest request) {
        Board board = access.getBoardOrThrow(boardId);
        access.requireOwner(board, userId);
        if (request.role() == BoardRole.OWNER) {
            throw new BadRequestException("Cannot assign OWNER role");
        }
        BoardMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));
        if (!member.getBoard().getId().equals(boardId)) {
            throw new BadRequestException("Member does not belong to this board");
        }
        member.setRole(request.role());
        return mapper.toMemberDto(memberRepository.save(member));
    }

    @Transactional
    public void removeMember(UUID userId, UUID boardId, UUID memberId) {
        Board board = access.getBoardOrThrow(boardId);
        access.requireOwner(board, userId);
        BoardMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));
        if (!member.getBoard().getId().equals(boardId)) {
            throw new BadRequestException("Member does not belong to this board");
        }
        memberRepository.delete(member);
    }
}
