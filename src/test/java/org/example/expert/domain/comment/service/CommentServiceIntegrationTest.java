package org.example.expert.domain.comment.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({CommentService.class})
@ActiveProfiles("test")
class CommentServiceIntegrationTest {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TodoRepository todoRepository;
    @Autowired
    private CommentService commentService;

    @Autowired
    private EntityManagerFactory managerFactory;
    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("JOIN FETCH를 통해 N+1 문제가 일어나지 않게 한다")
    void getComments_JOIN_FETCH로_불러와_문제가_일어나지_않게_한다() {
        //given
        User user = new User(
                "test@email.com",
                "password",
                UserRole.USER,
                "name"
        );
        userRepository.save(user);
        AuthUser auth = AuthUser.from(user);

        Todo todo = new Todo(
                "title",
                "content",
                "sunny",
                user
        );
        todoRepository.save(todo);

        CommentSaveRequest req = new CommentSaveRequest(
                "comment content"
        );
        CommentSaveRequest req2 = new CommentSaveRequest(
                "comment content2"
        );

        SessionFactory sessionFactory = managerFactory.unwrap(SessionFactory.class);

        Statistics statistics = sessionFactory.getStatistics();
        CommentSaveResponse savedRes = commentService.saveComment(auth, todo.getId(), req);
        commentService.saveComment(auth, todo.getId(), req2);

        //when
        entityManager.clear();
        statistics.clear();
        List<CommentResponse> res = commentService.getComments(todo.getId());

        //then
        assertThat(statistics.getPrepareStatementCount())
                .isEqualTo(1);
        assertThat(res.get(0).getId()).isEqualTo(savedRes.getId());
        assertThat(res.get(0).getContents()).isEqualTo(savedRes.getContents());
        assertThat(res.get(1).getUser().getId()).isEqualTo(user.getId());
    }
}