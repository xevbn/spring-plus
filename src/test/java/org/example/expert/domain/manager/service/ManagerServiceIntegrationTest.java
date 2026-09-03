package org.example.expert.domain.manager.service;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.log.Log;
import org.example.expert.domain.manager.log.LogRepository;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.support.MySQLContainerSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(MySQLContainerSupport.class)
@ActiveProfiles("test")
class ManagerServiceIntegrationTest {
    @Autowired
    private ManagerService managerService;
    @Autowired
    private ManagerRepository managerRepository;
    @Autowired
    private LogRepository logRepository;

    private static User user =  new User(
            "test@email.com",
            "password",
            UserRole.USER,
            "name"
    );
    private static User user2 =  new User(
            "test2@email.com",
            "password",
            UserRole.USER,
            "name2"
    );

    @BeforeAll
    static void init(@Autowired UserRepository userRepository, @Autowired TodoRepository todoRepository) {
        userRepository.saveAllAndFlush(List.of(user, user2));

        Todo todo = new Todo(
                "title",
                "content",
                "sunny",
                user
        );
        todoRepository.saveAndFlush(todo);
    }

    @BeforeEach
    void setUp() {
        AuthUser authUser = AuthUser.from(user);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(authUser, null, List.of()));
    }

    @Test
    @DisplayName("매니저 등록 성공 시 성공 로그가 남는다.")
    void saveManager_성공_시_성공_로그가_남는다() {
        //given
        AuthUser authUser = AuthUser.from(user);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(
                2L
        );

        //when
        managerService.saveManager(authUser, 1L, managerSaveRequest);
        List<Log> logs = logRepository.findAll();
        Log log = logs.get(logs.size() - 1);
        System.out.println("userId: " + log.getCurrentUserId() + " isFailed: " + log.isFailed());

        //then
        assertThat(log).isNotNull();
        assertThat(log.getCurrentUserId()).isEqualTo(user.getId());
        assertThat(log.getMessage()).isEqualTo("매니저 등록 요청");
        assertThat(log.isFailed()).isFalse();
    }

    @Test
    @DisplayName("매니저 등록 실패 시 실패 로그가 남는다.")
    void saveManager_실패_시_실패_로그가_남는다() {
        //given
        AuthUser authUser = AuthUser.from(user);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(
                2L
        );

        //when&then
        assertThatThrownBy(() -> managerService.saveManager(authUser, 2L, managerSaveRequest));
        List<Log> logs = logRepository.findAll();
        Log log = logs.get(logs.size() - 1);
        System.out.println("userId: " + log.getCurrentUserId() + " isFailed: " + log.isFailed());

        assertThat(log).isNotNull();
        assertThat(log.getCurrentUserId()).isEqualTo(user.getId());
        assertThat(log.getMessage()).isEqualTo("매니저 등록 요청");
        assertThat(log.isFailed()).isTrue();
    }
}