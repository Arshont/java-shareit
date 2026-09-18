package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.common.ShareItHeaders;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final String VALID_BODY =
            "{\"name\":\"Дрель\",\"description\":\"Аккумуляторная\",\"available\":true}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemClient itemClient;

    @Test
    void create_withValidBody_delegatesToClient() throws Exception {
        given(this.itemClient.create(anyLong(), any(NewItemDto.class)))
                .willReturn(ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", 1)));

        this.mockMvc.perform(post("/items")
                        .header(ShareItHeaders.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_withRequestId_delegatesToClient() throws Exception {
        given(this.itemClient.create(anyLong(), any(NewItemDto.class)))
                .willReturn(ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", 1)));

        this.mockMvc.perform(post("/items")
                        .header(ShareItHeaders.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Дрель\",\"description\":\"Аккумуляторная\","
                                + "\"available\":true,\"requestId\":5}"))
                .andExpect(status().isCreated());

        ArgumentCaptor<NewItemDto> captor = ArgumentCaptor.forClass(NewItemDto.class);
        verify(this.itemClient).create(eq(1L), captor.capture());
        assertThat(captor.getValue().getRequestId()).isEqualTo(5L);
    }

    @Test
    void create_withoutHeader_returns400() throws Exception {
        this.mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(this.itemClient, never()).create(anyLong(), any());
    }

    @Test
    void create_withoutName_returns400AndDoesNotCallClient() throws Exception {
        this.mockMvc.perform(post("/items")
                        .header(ShareItHeaders.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Аккумуляторная\",\"available\":true}"))
                .andExpect(status().isBadRequest());

        verify(this.itemClient, never()).create(anyLong(), any());
    }

    @Test
    void create_withoutDescription_returns400AndDoesNotCallClient() throws Exception {
        this.mockMvc.perform(post("/items")
                        .header(ShareItHeaders.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Дрель\",\"available\":true}"))
                .andExpect(status().isBadRequest());

        verify(this.itemClient, never()).create(anyLong(), any());
    }

    @Test
    void create_withoutAvailable_returns400AndDoesNotCallClient() throws Exception {
        this.mockMvc.perform(post("/items")
                        .header(ShareItHeaders.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Дрель\",\"description\":\"Аккумуляторная\"}"))
                .andExpect(status().isBadRequest());

        verify(this.itemClient, never()).create(anyLong(), any());
    }

    @Test
    void addComment_withBlankText_returns400AndDoesNotCallClient() throws Exception {
        this.mockMvc.perform(post("/items/1/comment")
                        .header(ShareItHeaders.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"   \"}"))
                .andExpect(status().isBadRequest());

        verify(this.itemClient, never()).addComment(anyLong(), anyLong(), any());
    }

    @Test
    void search_withoutTextParam_delegatesWithEmptyString() throws Exception {
        given(this.itemClient.search(anyLong(), any()))
                .willReturn(ResponseEntity.ok(java.util.List.of()));

        this.mockMvc.perform(get("/items/search")
                        .header(ShareItHeaders.USER_ID, 1L))
                .andExpect(status().isOk());

        verify(this.itemClient).search(1L, "");
    }

    @Test
    void update_delegatesToClient() throws Exception {
        given(this.itemClient.update(anyLong(), anyLong(), any(UpdateItemDto.class)))
                .willReturn(ResponseEntity.ok(Map.of("id", 1)));

        this.mockMvc.perform(patch("/items/7")
                        .header(ShareItHeaders.USER_ID, 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Дрель\"}"))
                .andExpect(status().isOk());

        verify(this.itemClient).update(eq(3L), eq(7L), any(UpdateItemDto.class));
    }

    @Test
    void getById_delegatesToClient() throws Exception {
        given(this.itemClient.getById(anyLong(), anyLong()))
                .willReturn(ResponseEntity.ok(Map.of("id", 7)));

        this.mockMvc.perform(get("/items/7").header(ShareItHeaders.USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));

        verify(this.itemClient).getById(1L, 7L);
    }

    @Test
    void getAllByOwner_withoutHeader_returns400AndDoesNotCallClient() throws Exception {
        this.mockMvc.perform(get("/items"))
                .andExpect(status().isBadRequest());

        verify(this.itemClient, never()).getAllByOwner(anyLong());
    }
}
