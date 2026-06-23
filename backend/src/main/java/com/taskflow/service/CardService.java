package com.taskflow.service;

import com.taskflow.dto.BoardEvent;
import com.taskflow.dto.CardDtos.CardDto;
import com.taskflow.dto.CardDtos.CreateCardRequest;
import com.taskflow.dto.CardDtos.MoveCardRequest;
import com.taskflow.dto.CardDtos.UpdateCardRequest;
import com.taskflow.entity.Board;
import com.taskflow.entity.Card;
import com.taskflow.entity.TaskList;
import com.taskflow.entity.User;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.NotFoundException;
import com.taskflow.mapper.DtoMapper;
import com.taskflow.repository.CardRepository;
import com.taskflow.repository.TaskListRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.websocket.BoardEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final TaskListRepository listRepository;
    private final UserRepository userRepository;
    private final BoardAccessService access;
    private final PositionService positionService;
    private final BoardEventPublisher events;
    private final ActivityService activityService;
    private final DtoMapper mapper;

    public CardService(CardRepository cardRepository,
                       TaskListRepository listRepository,
                       UserRepository userRepository,
                       BoardAccessService access,
                       PositionService positionService,
                       BoardEventPublisher events,
                       ActivityService activityService,
                       DtoMapper mapper) {
        this.cardRepository = cardRepository;
        this.listRepository = listRepository;
        this.userRepository = userRepository;
        this.access = access;
        this.positionService = positionService;
        this.events = events;
        this.activityService = activityService;
        this.mapper = mapper;
    }

    @Transactional
    public CardDto create(UUID userId, UUID boardId, UUID listId, CreateCardRequest request) {
        Board board = access.getBoardOrThrow(boardId);
        access.requireMember(board, userId);
        TaskList list = requireListOnBoard(listId, boardId);

        List<Card> existing = cardRepository.findByListIdOrderByPositionAsc(listId);
        Double last = existing.isEmpty() ? null : existing.get(existing.size() - 1).getPosition();
        double position = positionService.between(last, null);

        Card card = Card.builder()
                .title(request.title().trim())
                .description(request.description())
                .list(list)
                .position(position)
                .assignee(resolveAssignee(request.assigneeId()))
                .labels(request.labels())
                .dueDate(request.dueDate())
                .build();
        card = cardRepository.save(card);

        CardDto dto = mapper.toCardDto(card);
        activityService.record(board, userId, "added card \"" + card.getTitle() + "\"");
        events.publish(boardId, BoardEvent.of("CARD_CREATED", dto));
        return dto;
    }

    @Transactional
    public CardDto update(UUID userId, UUID boardId, UUID cardId, UpdateCardRequest request) {
        Board board = access.getBoardOrThrow(boardId);
        access.requireMember(board, userId);
        Card card = requireCardOnBoard(cardId, boardId);

        card.setTitle(request.title().trim());
        card.setDescription(request.description());
        card.setAssignee(resolveAssignee(request.assigneeId()));
        card.setLabels(request.labels());
        card.setDueDate(request.dueDate());
        card = cardRepository.save(card);

        CardDto dto = mapper.toCardDto(card);
        events.publish(boardId, BoardEvent.of("CARD_UPDATED", dto));
        return dto;
    }

    /**
     * Moves a card to {@code targetListId}, positioned between the supplied
     * neighbour cards. Position is the midpoint of the neighbours, so reordering
     * within a list and moving across lists share one code path.
     */
    @Transactional
    public CardDto move(UUID userId, UUID boardId, UUID cardId, MoveCardRequest request) {
        Board board = access.getBoardOrThrow(boardId);
        access.requireMember(board, userId);
        Card card = requireCardOnBoard(cardId, boardId);
        TaskList target = requireListOnBoard(request.targetListId(), boardId);

        Double before = cardPosition(request.beforeCardId(), boardId);
        Double after = cardPosition(request.afterCardId(), boardId);

        card.setList(target);
        card.setPosition(positionService.between(before, after));
        card = cardRepository.save(card);

        CardDto dto = mapper.toCardDto(card);
        events.publish(boardId, BoardEvent.of("CARD_MOVED", dto));
        return dto;
    }

    @Transactional
    public void delete(UUID userId, UUID boardId, UUID cardId) {
        Board board = access.getBoardOrThrow(boardId);
        access.requireMember(board, userId);
        Card card = requireCardOnBoard(cardId, boardId);
        cardRepository.delete(card);
        activityService.record(board, userId, "deleted card \"" + card.getTitle() + "\"");
        events.publish(boardId, BoardEvent.of("CARD_DELETED", cardId.toString()));
    }

    private User resolveAssignee(UUID assigneeId) {
        if (assigneeId == null) {
            return null;
        }
        return userRepository.findById(assigneeId)
                .orElseThrow(() -> new NotFoundException("Assignee not found"));
    }

    private TaskList requireListOnBoard(UUID listId, UUID boardId) {
        TaskList list = listRepository.findById(listId)
                .orElseThrow(() -> new NotFoundException("List not found"));
        if (!list.getBoard().getId().equals(boardId)) {
            throw new BadRequestException("List does not belong to this board");
        }
        return list;
    }

    private Card requireCardOnBoard(UUID cardId, UUID boardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found"));
        if (!card.getList().getBoard().getId().equals(boardId)) {
            throw new BadRequestException("Card does not belong to this board");
        }
        return card;
    }

    private Double cardPosition(UUID cardId, UUID boardId) {
        if (cardId == null) {
            return null;
        }
        return requireCardOnBoard(cardId, boardId).getPosition();
    }
}
