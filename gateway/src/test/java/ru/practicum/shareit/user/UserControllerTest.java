package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.NewUserDto;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserClient userClient;

    @Test
    void create_withValidBody_delegatesToClient() throws Exception {
        given(this.userClient.create(any(NewUserDto.class)))
                .willReturn(ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of("id", 1, "name", "Иван", "email", "ivan@example.com")));

        this.mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Иван\",\"email\":\"ivan@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ivan@example.com"));
    }

    @Test
    void create_withBlankName_returns400AndDoesNotCallClient() throws Exception {
        this.mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"ivan@example.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(this.userClient, never()).create(any());
    }

    @Test
    void create_withoutEmail_returns400AndDoesNotCallClient() throws Exception {
        this.mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Иван\"}"))
                .andExpect(status().isBadRequest());

        verify(this.userClient, never()).create(any());
    }

    @Test
    void create_withMalformedEmail_returns400AndDoesNotCallClient() throws Exception {
        this.mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Иван\",\"email\":\"не-почта\"}"))
                .andExpect(status().isBadRequest());

        verify(this.userClient, never()).create(any());
    }
}
