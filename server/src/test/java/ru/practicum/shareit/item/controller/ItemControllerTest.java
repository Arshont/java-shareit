package ru.practicum.shareit.item.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.common.ShareItHeaders;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final String BODY =
            "{\"name\":\"Дрель\",\"description\":\"Ударная\",\"available\":true}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    private static ItemDto itemDto() {
        return new ItemDto(1L, "Дрель", "Ударная", true, null);
    }

    @Test
    void create_returns201() throws Exception {
        given(this.itemService.create(anyLong(), any(NewItemDto.class))).willReturn(itemDto());

        this.mockMvc.perform(post("/items")
                        .header(ShareItHeaders.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void create_withoutHeader_returns400() throws Exception {
        this.mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void create_withUnknownRequestId_returns404() throws Exception {
        given(this.itemService.create(anyLong(), any(NewItemDto.class)))
                .willThrow(NotFoundException.itemRequest(9999L));

        this.mockMvc.perform(post("/items")
                        .header(ShareItHeaders.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Дрель\",\"description\":\"Ударная\","
                                + "\"available\":true,\"requestId\":9999}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Запрос вещи с id=9999 не найден"));
    }

    @Test
    void update_returns200() throws Exception {
        given(this.itemService.update(anyLong(), anyLong(), any(UpdateItemDto.class)))
                .willReturn(itemDto());

        this.mockMvc.perform(patch("/items/7")
                        .header(ShareItHeaders.USER_ID, 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Дрель\"}"))
                .andExpect(status().isOk());

        verify(this.itemService).update(eq(3L), eq(7L), any(UpdateItemDto.class));
    }

    @Test
    void update_byForeignUser_returns403() throws Exception {
        given(this.itemService.update(anyLong(), anyLong(), any(UpdateItemDto.class)))
                .willThrow(new ForbiddenException("Пользователь с id=2 не является владельцем вещи с id=1"));

        this.mockMvc.perform(patch("/items/1")
                        .header(ShareItHeaders.USER_ID, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Дрель\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getById_returnsItemWithComments() throws Exception {
        given(this.itemService.getById(anyLong(), anyLong())).willReturn(
                new ItemWithBookingsDto(1L, "Дрель", "Ударная", true, null, null, null, List.of()));

        this.mockMvc.perform(get("/items/7").header(ShareItHeaders.USER_ID, 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments").isArray());

        verify(this.itemService).getById(3L, 7L);
    }

    @Test
    void getAllByOwner_returnsList() throws Exception {
        given(this.itemService.getAllByOwner(anyLong())).willReturn(List.of(
                new ItemWithBookingsDto(1L, "Дрель", "Ударная", true, null, null, null, List.of())));

        this.mockMvc.perform(get("/items").header(ShareItHeaders.USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void search_passesTextToService() throws Exception {
        given(this.itemService.search(anyString())).willReturn(List.of(itemDto()));

        this.mockMvc.perform(get("/items/search")
                        .header(ShareItHeaders.USER_ID, 1L)
                        .param("text", "дрель"))
                .andExpect(status().isOk());

        verify(this.itemService).search("дрель");
    }

    @Test
    void addComment_returns201() throws Exception {
        given(this.itemService.addComment(anyLong(), anyLong(), any(NewCommentDto.class)))
                .willReturn(new CommentDto(1L, "Отличная дрель", "Иван",
                        LocalDateTime.of(2026, 9, 17, 12, 0, 0)));

        this.mockMvc.perform(post("/items/7/comment")
                        .header(ShareItHeaders.USER_ID, 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Отличная дрель\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorName").value("Иван"));

        verify(this.itemService).addComment(eq(3L), eq(7L), any(NewCommentDto.class));
    }

    @Test
    void addComment_withoutCompletedBooking_returns400() throws Exception {
        given(this.itemService.addComment(anyLong(), anyLong(), any(NewCommentDto.class)))
                .willThrow(new ValidationException("Нельзя оставить отзыв без завершённого бронирования"));

        this.mockMvc.perform(post("/items/1/comment")
                        .header(ShareItHeaders.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Отличная дрель\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}
