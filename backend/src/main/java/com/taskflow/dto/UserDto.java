package com.taskflow.dto;

import java.util.UUID;

public record UserDto(
        UUID id,
        String email,
        String displayName
) {
}
