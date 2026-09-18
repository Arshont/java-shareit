package ru.practicum.shareit.user.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private static UserDto userDto() {
        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setName("Иван");
        dto.setEmail("ivan@example.com");
        return dto;
    }

    @Test
    void create_returns201WithBody() throws Exception {
        given(this.userService.create(any(NewUserDto.class))).willReturn(userDto());

        this.mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Иван\",\"email\":\"ivan@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Иван"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"));
    }

    @Test
    void getAll_returnsList() throws Exception {
        given(this.userService.getAll()).willReturn(List.of(userDto()));

        this.mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("ivan@example.com"));
    }

    @Test
    void getById_returnsUser() throws Exception {
        given(this.userService.getById(1L)).willReturn(userDto());

        this.mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_whenServiceThrowsNotFound_returns404WithErrorBody() throws Exception {
        given(this.userService.getById(anyLong())).willThrow(NotFoundException.user(9999L));

        this.mockMvc.perform(get("/users/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь с id=9999 не найден"));
    }

    @Test
    void update_returnsUpdatedUser() throws Exception {
        given(this.userService.update(anyLong(), any(UpdateUserDto.class))).willReturn(userDto());

        this.mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Иван\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Иван"));
    }

    @Test
    void update_whenEmailTaken_returns409() throws Exception {
        given(this.userService.update(anyLong(), any(UpdateUserDto.class)))
                .willThrow(new ConflictException("Почта уже занята"));

        this.mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"taken@example.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Почта уже занята"));
    }

    @Test
    void delete_returns204() throws Exception {
        this.mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(this.userService).delete(1L);
    }

    @Test
    void delete_whenServiceThrowsUnexpected_returns500() throws Exception {
        willThrow(new IllegalStateException("что-то пошло не так"))
                .given(this.userService).delete(anyLong());

        this.mockMvc.perform(delete("/users/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("что-то пошло не так"));
    }
}
