package org.example.expert.domain.todo;

import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.User;

import java.util.List;

public class TodoFixture {
    private static final List<String> weathers = List.of("sunny", "rainy", "cloudy", "foggy");

    public static Todo create(User user, int i) {
        return new Todo(
                "title " + i,
                "content " + i,
                weathers.get(i % weathers.size()),
                user
        );
    }
}
