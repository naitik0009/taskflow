package com.taskflow.service;

import com.taskflow.dto.BoardEvent;
import com.taskflow.dto.ListDtos.CreateListRequest;
import com.taskflow.dto.ListDtos.ListDto;
import com.taskflow.dto.ListDtos.MoveListRequest;
import com.taskflow.dto.ListDtos.UpdateListRequest;
import com.taskflow.entity.Board;
import com.taskflow.entity.Card;
import com.taskflow.entity.TaskList;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.NotFoundException;
import com.taskflow.mapper.DtoMapper;
import com.taskflow.repository.CardRepository;
import com.taskflow.repository.TaskListRepository;
import com.taskflow.websocket.BoardEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListService {

    private final TaskListRepository listRepository;
    private final CardRepository cardRepository;
    private final BoardAccessService access;
    private final PositionService positionService;
    private final BoardEventPublisher events;
    private final ActivityService activityService;
    private final DtoMapper mapper;

    public ListService(TaskListRepository listRepository,
                       CardRepository cardRepository,
                       BoardAccessService access,
                       PositionService positionService,
                       BoardEventPublisher events,
                       ActivityService activityService,
                       DtoMapper mapper) {
        this.listRepository = listRepository;
        this.cardRepository = cardRepository;
        this.access = access;
        this.positionService = positionService;
        this.events = events;
        this.activityService = activityService;
        this.mapper = mapper;
    }

    @Transactional
    public ListDto create(UUID userId, UUID boardId, CreateListRequest request) {
        Board board = access.getBoardOrThrow(boardId);
        access.requireMember(board, userId);

        List<TaskList> existing = listRepository.findByBoardIdOrderByPositionAsc(boardId);
        Double last = existing.isEmpty() ? null : existing.get(existing.size() - 1).getPosition();
        double position = positionService.between(last, null);

        TaskList list = TaskList.builder()
                .name(request.name().trim())
                .board(board)
                .position(position)
                .build();
        list = listRepository.save(list);

        ListDto dto = mapper.toListDto(list, List.of());
        activityService.record(board, userId, "added list \"" + list.getName() + "\"");
        events.publish(boardId, BoardEvent.of("LIST_CREATED", dto));
        return dto;
    }

    @Transactional
    public ListDto rename(UUID userId, UUID boardId, UUID listId, UpdateListRequest request) {
        Board board = access.getBoardOrThrow(boardId);
        access.requireMember(board, userId);
        TaskList list = requireListOnBoard(listId, boardId);
        list.setName(request.name().trim());
        list = listRepository.save(list);

        List<Card> cards = cardRepository.findByListIdOrderByPositionAsc(listId);
        ListDto dto = mapper.toListDto(list, cards);
        events.publish(boardId, BoardEvent.of("LIST_UPDATED", dto));
        return dto;
    }

    @Transactional
    public ListDto move(UUID userId, UUID boardId, UUID listId, MoveListRequest request) {
        Board board = access.getBoardOrThrow(boardId);
        access.requireMember(board, userId);
        TaskList list = requireListOnBoard(listId, boardId);

        Double before = positionOf(request.beforeListId(), boardId);
        Double after = positionOf(request.afterListId(), boardId);
        list.setPosition(positionService.between(before, after));
        list = listRepository.save(list);

        List<Card> cards = cardRepository.findByListIdOrderByPositionAsc(listId);
        ListDto dto = mapper.toListDto(list, cards);
        events.publish(boardId, BoardEvent.of("LIST_MOVED", dto));
        return dto;
    }

    @Transactional
    public void delete(UUID userId, UUID boardId, UUID listId) {
        Board board = access.getBoardOrThrow(boardId);
        access.requireMember(board, userId);
        TaskList list = requireListOnBoard(listId, boardId);
        cardRepository.deleteAll(cardRepository.findByListIdOrderByPositionAsc(listId));
        listRepository.delete(list);
        activityService.record(board, userId, "deleted list \"" + list.getName() + "\"");
        events.publish(boardId, BoardEvent.of("LIST_DELETED", listId.toString()));
    }

    private TaskList requireListOnBoard(UUID listId, UUID boardId) {
        TaskList list = listRepository.findById(listId)
                .orElseThrow(() -> new NotFoundException("List not found"));
        if (!list.getBoard().getId().equals(boardId)) {
            throw new BadRequestException("List does not belong to this board");
        }
        return list;
    }

    private Double positionOf(UUID listId, UUID boardId) {
        if (listId == null) {
            return null;
        }
        return requireListOnBoard(listId, boardId).getPosition();
    }
}
