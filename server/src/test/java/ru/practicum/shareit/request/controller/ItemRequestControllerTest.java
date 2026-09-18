package ru.practicum.shareit.request.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.common.ShareItHeaders;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemAnswerDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    private static final LocalDateTime CREATED = LocalDateTime.of(2026, 9, 17, 12, 0, 0);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void create_returns201WithCreatedField() throws Exception {
        given(this.itemRequestService.create(anyLong(), any(NewItemRequestDto.class)))
                .willReturn(new ItemRequestDto(1L, "нужна дрель", CREATED));

        this.mockMvc.perform(post("/requests")
                        .header(ShareItHeaders.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"нужна дрель\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("нужна дрель"))
                .andExpect(jsonPath("$.created").value("2026-09-17T12:00:00"));
    }

    @Test
    void create_withUnknownUser_returns404() throws Exception {
        given(this.itemRequestService.create(anyLong(), any(NewItemRequestDto.class)))
                .willThrow(NotFoundException.user(9999L));

        this.mockMvc.perform(post("/requests")
                        .header(ShareItHeaders.USER_ID, 9999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"нужна дрель\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void create_withoutHeader_returns400() throws Exception {
        this.mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"нужна дрель\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getOwn_returnsRequestsWithItems() throws Exception {
        given(this.itemRequestService.getOwn(anyLong())).willReturn(List.of(
                new ItemRequestWithItemsDto(1L, "нужна дрель", CREATED,
                        List.of(new ItemAnswerDto(3L, "Дрель Bosch", 2L)))));

        this.mockMvc.perform(get("/requests").header(ShareItHeaders.USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].items[0].name").value("Дрель Bosch"))
                .andExpect(jsonPath("$[0].items[0].ownerId").value(2));
    }

    @Test
    void getAll_returnsRequestsWithoutItemsField() throws Exception {
        given(this.itemRequestService.getAll(anyLong()))
                .willReturn(List.of(new ItemRequestDto(1L, "нужна дрель", CREATED)));

        this.mockMvc.perform(get("/requests/all").header(ShareItHeaders.USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].items").doesNotExist());

        verify(this.itemRequestService).getAll(1L);
    }

    @Test
    void getById_returnsSingleRequestWithItems() throws Exception {
        given(this.itemRequestService.getById(anyLong(), anyLong())).willReturn(
                new ItemRequestWithItemsDto(7L, "нужна дрель", CREATED,
                        List.of(new ItemAnswerDto(3L, "Дрель Bosch", 2L))));

        this.mockMvc.perform(get("/requests/7").header(ShareItHeaders.USER_ID, 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.items[0].id").value(3));

        verify(this.itemRequestService).getById(5L, 7L);
    }

    @Test
    void getById_withUnknownRequest_returns404() throws Exception {
        given(this.itemRequestService.getById(anyLong(), anyLong()))
                .willThrow(NotFoundException.itemRequest(9999L));

        this.mockMvc.perform(get("/requests/9999").header(ShareItHeaders.USER_ID, 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Запрос вещи с id=9999 не найден"));
    }
}
