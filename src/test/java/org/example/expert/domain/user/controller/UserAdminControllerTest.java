package org.example.expert.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.aop.AdminAccessLoggingAspect;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.service.UserAdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserAdminController.class)
@Import(AdminAccessLoggingAspect.class)
@EnableAspectJAutoProxy
public class UserAdminControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @SpyBean
    private AdminAccessLoggingAspect loggingAspect;
    @MockBean
    private UserAdminService userAdminService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("UserAdminController.changeUserRole 메서드 호출 시 AdminAccessLoggingAspect가 동작해야 한다")
    void changeUserRole_메서드가_동작할_때_AOP가_동작해_로그를_남겨야_한다() throws Exception {
        //given
        UserRoleChangeRequest req = new UserRoleChangeRequest(
                "USER"
        );

        InOrder inOrder = inOrder(loggingAspect, userAdminService);

        //when&then
        mockMvc.perform(patch("/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
        inOrder.verify(loggingAspect).logBeforeChangeUserRole(any());
        inOrder.verify(userAdminService).changeUserRole(anyLong(), any());
    }
}
