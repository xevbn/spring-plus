package org.example.expert.domain.todo.repository.dto;

import java.time.LocalDateTime;

public record TodoSearchConditions(
    String title,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String managerName
) {
}
