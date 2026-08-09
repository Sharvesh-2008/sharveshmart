package com.digitalmarketplace.controller;

import com.digitalmarketplace.entity.User;
import com.digitalmarketplace.entity.UserRole;
import com.digitalmarketplace.exception.ResourceNotFoundException;
import com.digitalmarketplace.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private User user(long id) {
        User user = new User();
        user.setId(id);
        user.setName("Alice");
        user.setEmail("alice@example.com");
        user.setRole(UserRole.SELLER);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    @Test
    void getUserReturnsUser() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(user(1L)));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.role").value("SELLER"));
    }

    @Test
    void getUserWhenMissingReturnsNotFound() throws Exception {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserWithNonPositiveIdReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/users/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserWhenServiceThrowsNotFoundReturnsNotFound() throws Exception {
        when(userService.findById(1L)).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound());
    }
}
