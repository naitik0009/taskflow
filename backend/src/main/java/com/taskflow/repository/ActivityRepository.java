package com.taskflow.repository;

import com.taskflow.entity.Activity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {
    List<Activity> findByBoardIdOrderByCreatedAtDesc(UUID boardId, Pageable pageable);
}
