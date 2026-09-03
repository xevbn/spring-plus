package org.example.expert.domain.todo.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.user.dto.response.UserResponse;

import java.util.Optional;

import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class TodoQuerydslRepositoryImpl implements TodoQuerydslRepository {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<TodoResponse> findByIdWithUser(long todoId) {
        return Optional.ofNullable(jpaQueryFactory
                .select(Projections.constructor(TodoResponse.class,
                        todo.id,
                        todo.title,
                        todo.contents,
                        todo.weather,
                        Projections.constructor(UserResponse.class,
                                user.id,
                                user.email),
                        todo.createdAt,
                        todo.modifiedAt))
                .from(todo)
                .leftJoin(todo.user, user)
                .where(
                        idEq(todoId)
                )
                .fetchOne());
    }

    private BooleanExpression idEq(long id) {
        return todo.id.eq(id);
    }
}
