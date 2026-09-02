package org.example.expert.domain.todo.repository;

import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class TodoRepositoryTest {
    @Autowired
    private TodoRepository todoRepository;
    @Autowired
    private ManagerRepository managerRepository;
    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setup() {
        user = new User(
                "test@email.com",
                "password",
                UserRole.USER,
                "name"
        );

        userRepository.save(user);
    }

    @Test
    @DisplayName("Todo 저장 시 Manager도 같이 저장되어야 한다 (Cascade.PERSIST)")
    void save_todo를_저장하면_Manager도_같이_저장되어야_한다() {
        //given
        Todo todo = new Todo(
                "title",
                "content",
                "sunny",
                user
        );

        //when
        Todo saved = todoRepository.save(todo);

        //then
        List<Manager> managers = managerRepository.findByTodoIdWithUser(saved.getId());
        assertThat(saved.getManagers().get(0)).isEqualTo(managers.get(0));
        assertThat(managers.get(0).getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("Todo 삭제 시 Manager도 같이 삭제되어야 한다 (Cascade.REMOVE)")
    void save_todo를_삭제하면_Manager도_같이_삭제되어야_한다() {
        //given
        Todo todo = new Todo(
                "title",
                "content",
                "sunny",
                user
        );
        Todo saved = todoRepository.save(todo);

        //when
        todoRepository.delete(saved);

        //then
        List<Manager> managers = managerRepository.findByTodoIdWithUser(saved.getId());
        assertThat(managers.size()).isEqualTo(0);
    }
}
