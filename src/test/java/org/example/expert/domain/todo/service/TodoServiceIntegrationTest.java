package org.example.expert.domain.todo.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.config.PersistenceConfig;
import org.example.expert.config.QueryDSLConfig;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.TodoFixture;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoQuerydslRepository;
import org.example.expert.domain.todo.repository.TodoQuerydslRepositoryImpl;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.support.MySQLContainerSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@DataJpaTest
@Import({
        MySQLContainerSupport.class,
        TodoService.class,
        PersistenceConfig.class,
        QueryDSLConfig.class
})
public class TodoServiceIntegrationTest {
    @MockBean
    private WeatherClient weatherClient;
    @Autowired
    private TodoService todoService;
    @SpyBean
    private TodoRepository todoRepository;
    @Autowired
    private UserRepository userRepository;

    User user;

    @BeforeEach
    public void setup() {
        user = new User(
                "test@email.com",
                "password",
                UserRole.USER,
                "nickname"
        );

        userRepository.saveAndFlush(user);


        List<Todo> todos = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            todos.add(TodoFixture.create(user, i));
        }

        todoRepository.saveAll(todos);
    }

    @Test
    @DisplayName("saveTodo Transaction 테스트")
    void save_todo_트랜잭션_readOnly_아님() {
        //given
        AuthUser authUser = new AuthUser(
                user.getId(),
                "test@email.com",
                UserRole.USER
        );

        TodoSaveRequest request = new TodoSaveRequest(
                "title",
                "content"
        );

        //when
        TodoSaveResponse res = todoService.saveTodo(authUser, request);

        //then
        verify(todoRepository).save(any(Todo.class));
        assertThat(res.getTitle()).isEqualTo("title");
    }

    @Test
    @DisplayName("getTodos 동적 조건 조회 테스트 - 모든 조건이 있는 경우")
    void get_todos_동적_조건_조회_테스트_모든_조건() {
        //when
        Page<TodoResponse> todos = todoService.getTodos(1, 10, "sunny",
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1));

        //then
        assertThat(todos.getSize()).isGreaterThan(0);
        assertThat(todos.getTotalPages()).isEqualTo(3);
        assertThat(todos.getTotalElements()).isEqualTo(25);
        assertThat(todos.map(TodoResponse::getWeather)).allMatch(Predicate.isEqual("sunny"));
    }

    @Test
    @DisplayName("getTodos 동적 조건 조회 테스트 - 종료 일시 없을 때")
    void get_todos_동적_조건_조회_테스트_종료_일시_없음() {
        //when
        Page<TodoResponse> todos = todoService.getTodos(1, 10, "sunny",
                LocalDateTime.now().minusHours(1), null);

        //then
        assertThat(todos.getSize()).isGreaterThan(0);
        assertThat(todos.getTotalPages()).isEqualTo(3);
        assertThat(todos.getTotalElements()).isEqualTo(25);
        assertThat(todos.map(TodoResponse::getWeather)).allMatch(Predicate.isEqual("sunny"));
    }

    @Test
    @DisplayName("getTodos 동적 조건 조회 테스트 - 날씨 조건 없음")
    void get_todos_동적_조건_조회_테스트_날씨_조건_없음() {
        //when
        Page<TodoResponse> todos = todoService.getTodos(1, 10, null,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1));

        //then
        assertThat(todos.getSize()).isGreaterThan(0);
        assertThat(todos.getTotalPages()).isEqualTo(10);
        assertThat(todos.getTotalElements()).isEqualTo(100);
    }

    @Test
    @DisplayName("getTodo querydsl 적용 테스트")
    void getTodo_querydsl_적용() {
        //when
        TodoResponse res = todoService.getTodo(12L);

        //then
        //fixture가 0부터 시작해서 id보다 1 작음
        assertThat(res.getTitle()).isEqualTo("title 11");
        assertThat(res.getContents()).isEqualTo("content 11");
    }
}
