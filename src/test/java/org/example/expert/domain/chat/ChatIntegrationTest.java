package org.example.expert.domain.chat;

import org.example.expert.domain.chat.dto.ChatMessageDto;
import org.example.expert.domain.chat.dto.ChatSendRequest;
import org.example.expert.domain.chat.service.ChatWebSocketHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChatIntegrationTest {
    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;

    private StompSession clientA;
    private StompSession clientB;
    @Autowired
    private ChatWebSocketHandler webSocketHandler;

    @BeforeEach
    void setUp() {
        StandardWebSocketClient webSocketClient = new StandardWebSocketClient();

        stompClient = new WebSocketStompClient(webSocketClient);

        stompClient.setMessageConverter(
                new MappingJackson2MessageConverter()
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        if (clientA != null) {
            clientA.disconnect();
        }
        if (clientB != null) {
            clientB.disconnect();
        }
    }

    @Test
    @DisplayName("두 클라이언트 간 웹소켓으로 메시지 송/수신")
    void 두_클라이언트가_웹소켓으로_메시지를_주고_받는다()  throws Exception {
        //given
        String url = "ws://localhost:" + port + "/ws";

        clientA = stompClient.connectAsync(
                url,
                new StompSessionHandlerAdapter() {
                }
        ).get(5, TimeUnit.SECONDS);

        clientB = stompClient.connectAsync(
                url,
                new StompSessionHandlerAdapter() {
                }
        ).get(5, TimeUnit.SECONDS);

        CompletableFuture<ChatMessageDto> received = new CompletableFuture<>();

        clientB.subscribe(
                "/sub/chat",
                new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return ChatMessageDto.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        received.complete((ChatMessageDto) payload);
                    }
                }
        );

        ChatSendRequest payload = new ChatSendRequest(
                "hello"
        );

        clientA.send(
                "/pub/chat.send",
                payload
        );

        //then
        ChatMessageDto message = received.get(5, TimeUnit.SECONDS);

        assertThat(message.content()).isEqualTo("hello");
    }
}
