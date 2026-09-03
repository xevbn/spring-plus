package org.example.expert.domain.manager.log;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long currentUserId;
    private String message;
    private LocalDateTime createdAt;
    private boolean failed;

    private Log(String message, Long currentUserId, boolean failed) {
        this.message = message;
        this.currentUserId = currentUserId;
        this.failed = failed;
        this.createdAt = LocalDateTime.now();
    }

    public static Log createLog(Long currentUserId, String message, boolean failed) {
        return new Log(
                message,
                currentUserId,
                failed
        );
    }
}
