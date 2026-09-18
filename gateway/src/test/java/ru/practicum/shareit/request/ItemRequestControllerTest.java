package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.common.ShareItHeaders;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Test
    void create_withValidBody_delegatesToClient() throws Exception {
        given(this.itemRequestClient.create(anyLong(), any(NewItemRequestDto.class)))
                .willReturn(ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of("id", 1, "description", "нужна дрель")));

        this.mockMvc.perform(post("/requests")
                        .header(ShareItHeaders.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"нужна дрель\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("нужна дрель"));

        verify(this.itemRequestClient).create(eq(1L), any(NewItemRequestDto.class));
    }

    @Test
    void create_withBlankDescription_returns400AndDoesNotCallClient() throws Exception {
        this.mockMvc.perform(post("/requests")
                        .header(ShareItHeaders.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(this.itemRequestClient, never()).create(anyLong(), any());
    }

    @Test
    void create_withoutHeader_returns400() throws Exception {
        this.mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"нужна дрель\"}"))
                .andExpect(status().isBadRequest());

        verify(this.itemRequestClient, never()).create(anyLong(), any());
    }

    @Test
    void getOwn_delegatesToClient() throws Exception {
        given(this.itemRequestClient.getOwn(anyLong())).willReturn(ResponseEntity.ok(List.of()));

        this.mockMvc.perform(get("/requests").header(ShareItHeaders.USER_ID, 1L))
                .andExpect(status().isOk());

        verify(this.itemRequestClient).getOwn(1L);
    }

    @Test
    void getAll_delegatesToClient() throws Exception {
        given(this.itemRequestClient.getAll(anyLong())).willReturn(ResponseEntity.ok(List.of()));

        this.mockMvc.perform(get("/requests/all").header(ShareItHeaders.USER_ID, 1L))
                .andExpect(status().isOk());

        verify(this.itemRequestClient).getAll(1L);
    }

    @Test
    void getById_delegatesToClient() throws Exception {
        given(this.itemRequestClient.getById(anyLong(), anyLong()))
                .willReturn(ResponseEntity.ok(Map.of("id", 7)));

        this.mockMvc.perform(get("/requests/7").header(ShareItHeaders.USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));

        verify(this.itemRequestClient).getById(1L, 7L);
    }
}
