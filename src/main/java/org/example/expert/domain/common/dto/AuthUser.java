package org.example.expert.domain.common.dto;

import lombok.Getter;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;

@Getter
public class AuthUser {

    private final Long id;
    private final String email;
    private final UserRole userRole;

    public AuthUser(Long id, String email, UserRole userRole) {
        this.id = id;
        this.email = email;
        this.userRole = userRole;
    }

    public static AuthUser from(User user) {
        return new AuthUser(
                user.getId(),
                user.getEmail(),
                user.getUserRole()
        );
    }
}
