package org.example.expert.domain.todo.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@DataJpaTest
@Import({MySQLContainerSupport.class, TodoService.class})
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

        userRepository.save(user);
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
}
