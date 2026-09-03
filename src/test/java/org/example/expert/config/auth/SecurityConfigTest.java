package org.example.expert.config.auth;

import com.github.dockerjava.api.model.AuthResponse;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTest {
    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("permitAll로 지정된 url에는 토큰없이 접근할 수 있다.")
    void auth_api는_인증_없이도_접근할_수_있다() throws Exception {
        //given
        SignupRequest signupRequest = new SignupRequest(
                "test@email.com",
                "name",
                "password",
                "USER"
        );

        //when&then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bearerToken").exists());
    }

    @Test
    @DisplayName("인증이 필요한 url에는 토큰 없이 접근할 수 없다.")
    void todo_api는_인증_없으면_400_에러를_보낸다() throws Exception {
        //given
        TodoSaveRequest req =  new TodoSaveRequest(
                "title",
                "contents"
        );

        //when&then
        mockMvc.perform(post("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("인증이 필요한 url에는 토큰이 있으면 접근할 수 있다.")
    void todo_api는_토큰이_있으면_접근할_수_있다() throws Exception {
        //given
        TodoSaveRequest req =  new TodoSaveRequest(
                "title",
                "contents"
        );

        SignupRequest signupRequest = new SignupRequest(
                "test@email.com",
                "name",
                "password",
                "USER"
        );

        MvcResult result = mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bearerToken").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode json = objectMapper.readTree(responseBody);
        String token  = json.get("bearerToken").asText();

        //when&then
        mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }
}
