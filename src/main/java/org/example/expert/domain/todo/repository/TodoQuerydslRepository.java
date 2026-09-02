package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.dto.response.TodoResponse;

import java.util.Optional;

public interface TodoQuerydslRepository {
    Optional<TodoResponse> findByIdWithUser(long todoId);
}
