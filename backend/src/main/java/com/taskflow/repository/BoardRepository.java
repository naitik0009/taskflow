package com.taskflow.repository;

import com.taskflow.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BoardRepository extends JpaRepository<Board, UUID> {

    @Query("""
            select distinct b from Board b
            left join BoardMember m on m.board = b
            where b.owner.id = :userId or m.user.id = :userId
            order by b.createdAt desc
            """)
    List<Board> findAllForUser(@Param("userId") UUID userId);
}
