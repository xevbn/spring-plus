package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.repository.dto.TodoSearchConditions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TodoQuerydslRepository {
    Optional<TodoResponse> findByIdWithUser(long todoId);
    Page<TodoSearchResponse> findBySearchConditions(Pageable pageable, TodoSearchConditions conditions);
}
