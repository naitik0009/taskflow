package com.taskflow.dto;

import java.time.Instant;
import java.util.UUID;

public record ActivityDto(
        UUID id,
        UserDto actor,
        String message,
        Instant createdAt
) {
}
