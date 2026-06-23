package com.taskflow.service;

import com.taskflow.dto.ActivityDto;
import com.taskflow.entity.Activity;
import com.taskflow.entity.Board;
import com.taskflow.entity.User;
import com.taskflow.mapper.DtoMapper;
import com.taskflow.repository.ActivityRepository;
import com.taskflow.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final DtoMapper mapper;

    public ActivityService(ActivityRepository activityRepository,
                           UserRepository userRepository,
                           DtoMapper mapper) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Transactional
    public Activity record(Board board, UUID actorId, String message) {
        User actor = userRepository.getReferenceById(actorId);
        Activity activity = Activity.builder()
                .board(board)
                .actor(actor)
                .message(message)
                .build();
        return activityRepository.save(activity);
    }

    @Transactional(readOnly = true)
    public List<ActivityDto> recent(UUID boardId, int limit) {
        return activityRepository
                .findByBoardIdOrderByCreatedAtDesc(boardId, PageRequest.of(0, limit))
                .stream()
                .map(mapper::toActivityDto)
                .toList();
    }
}
