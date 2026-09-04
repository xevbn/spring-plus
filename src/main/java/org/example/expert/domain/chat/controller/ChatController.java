package org.example.expert.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.chat.dto.ChatMessageDto;
import org.example.expert.domain.chat.dto.ChatSendRequest;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatSendRequest payload, SimpMessageHeaderAccessor accessor) {
        String userId = (String) accessor.getSessionAttributes().get("userId");
        log.info(userId);

        ChatMessageDto message = new ChatMessageDto(
                userId,
                payload.message()
        );

        messagingTemplate.convertAndSend("/sub/chat", message);
    }
}
