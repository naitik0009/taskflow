package com.taskflow.repository;

import com.taskflow.entity.BoardMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardMemberRepository extends JpaRepository<BoardMember, UUID> {
    Optional<BoardMember> findByBoardIdAndUserId(UUID boardId, UUID userId);
    List<BoardMember> findByBoardId(UUID boardId);
    boolean existsByBoardIdAndUserId(UUID boardId, UUID userId);
}
