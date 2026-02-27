package com.pos.controller;

import com.pos.dto.response.UserResponse;
import com.pos.service.AuthService;
import com.pos.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private AuthService authService;

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getAllUsers_withPagination_returnsPage() throws Exception {
        UserResponse user = UserResponse.builder()
                .id(1L)
                .username("admin")
                .email("admin@pos.com")
                .role(com.pos.enums.Role.ADMIN)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        when(userService.getAllUsers(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user), org.springframework.data.domain.PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/users").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].username").value("admin"))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.number").value(0));
    }

    @Test
    @WithMockUser(authorities = "MANAGER")
    void getAllUsers_nonAdmin_returns403() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_unauthenticated_returns401Or403() throws Exception {
        var result = mockMvc.perform(get("/api/users")).andReturn();
        int status = result.getResponse().getStatus();
        assertThat(status).isIn(401, 403);
    }
}
