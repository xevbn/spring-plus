package org.example.expert.domain.todo.dto.response;

public record TodoSearchResponse(
        String title,
        int managerCount,
        int commentCount
) {
}
