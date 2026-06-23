package com.taskflow.dto;

import com.taskflow.entity.BoardRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class BoardDtos {

    private BoardDtos() {
    }

    public record CreateBoardRequest(
            @NotBlank @Size(max = 120) String name,
            @Size(max = 1000) String description
    ) {
    }

    public record UpdateBoardRequest(
            @NotBlank @Size(max = 120) String name,
            @Size(max = 1000) String description
    ) {
    }

    public record BoardSummary(
            UUID id,
            String name,
            String description,
            UserDto owner,
            BoardRole role,
            Instant createdAt
    ) {
    }

    public record BoardDetail(
            UUID id,
            String name,
            String description,
            UserDto owner,
            BoardRole role,
            List<MemberDto> members,
            List<ListDtos.ListDto> lists,
            Instant createdAt
    ) {
    }

    public record MemberDto(
            UUID id,
            UserDto user,
            BoardRole role
    ) {
    }

    public record InviteMemberRequest(
            @NotBlank @Email String email,
            @NotNull BoardRole role
    ) {
    }

    public record UpdateMemberRoleRequest(
            @NotNull BoardRole role
    ) {
    }
}
