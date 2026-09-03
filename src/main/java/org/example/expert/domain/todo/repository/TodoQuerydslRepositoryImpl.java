package org.example.expert.domain.todo.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.repository.dto.TodoSearchConditions;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.springframework.util.StringUtils.hasText;

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

    @Override
    public Page<TodoSearchResponse> findBySearchConditions(Pageable pageable, TodoSearchConditions conditions) {
        BooleanBuilder condition = allConditions(conditions);

        List<TodoSearchResponse> content = jpaQueryFactory
                .select(Projections.constructor(TodoSearchResponse.class,
                        todo.title,
                        todo.managers.size().castToNum(Integer.class),
                        todo.comments.size().castToNum(Integer.class)))
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                .where(
                        condition
                )
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = jpaQueryFactory.select(todo.count())
                .from(todo)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private BooleanExpression idEq(long id) {
        return todo.id.eq(id);
    }

    private BooleanBuilder allConditions(TodoSearchConditions conditions) {
        return new BooleanBuilder()
                .and(titleLike(conditions.title()))
                .and(createdAtBetween(conditions.startTime(), conditions.endTime()))
                .and(managerNameLike(conditions.managerName()));
    }

    private BooleanExpression titleLike(String title) {
        return hasText(title) ? todo.title.contains(title) : null;
    }

    private BooleanExpression createdAtBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null && end == null) {
            return null;
        } else if (start != null && end == null) {
            return todo.createdAt.goe(start);
        } else if (start == null) {
            return todo.createdAt.loe(end);
        } else {
            return todo.createdAt.between(start, end);
        }
    }

    private BooleanExpression managerNameLike(String managerName) {
        return hasText(managerName) ? user.nickname.contains(managerName) : null;
    }
}
