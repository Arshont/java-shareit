package ru.practicum.shareit.booking.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.common.ShareItHeaders;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String BODY =
            "{\"itemId\":1,\"start\":\"2030-01-01T10:00:00\",\"end\":\"2030-01-02T10:00:00\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    private static BookingDto bookingDto(BookingStatus status) {
        UserDto booker = new UserDto();
        booker.setId(2L);
        booker.setName("Арендатор");
        booker.setEmail("booker@example.com");
        return new BookingDto(
                1L,
                LocalDateTime.of(2030, 1, 1, 10, 0, 0),
                LocalDateTime.of(2030, 1, 2, 10, 0, 0),
                status,
                booker,
                new ItemDto(1L, "Дрель", "Ударная", true, null));
    }

    @Test
    void create_returns201WithWaitingStatus() throws Exception {
        given(this.bookingService.create(anyLong(), any(NewBookingDto.class)))
                .willReturn(bookingDto(BookingStatus.WAITING));

        this.mockMvc.perform(post("/bookings")
                        .header(ShareItHeaders.USER_ID, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(2))
                .andExpect(jsonPath("$.item.name").value("Дрель"))
                .andExpect(jsonPath("$.start").value("2030-01-01T10:00:00"));
    }

    @Test
    void create_withUnknownItem_returns404() throws Exception {
        given(this.bookingService.create(anyLong(), any(NewBookingDto.class)))
                .willThrow(NotFoundException.item(9999L));

        this.mockMvc.perform(post("/bookings")
                        .header(ShareItHeaders.USER_ID, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void create_withoutHeader_returns400() throws Exception {
        this.mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void approve_returns200WithApprovedStatus() throws Exception {
        given(this.bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                .willReturn(bookingDto(BookingStatus.APPROVED));

        this.mockMvc.perform(patch("/bookings/7")
                        .header(ShareItHeaders.USER_ID, 3L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(this.bookingService).approve(3L, 7L, true);
    }

    @Test
    void approve_byNonOwner_returns403() throws Exception {
        given(this.bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                .willThrow(new ForbiddenException("Подтверждать может только владелец"));

        this.mockMvc.perform(patch("/bookings/1")
                        .header(ShareItHeaders.USER_ID, 3L)
                        .param("approved", "true"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Подтверждать может только владелец"));
    }

    @Test
    void getById_returns200() throws Exception {
        given(this.bookingService.getById(anyLong(), anyLong()))
                .willReturn(bookingDto(BookingStatus.APPROVED));

        this.mockMvc.perform(get("/bookings/7").header(ShareItHeaders.USER_ID, 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(this.bookingService).getById(3L, 7L);
    }

    @Test
    void getAllByBooker_withoutStateParam_passesAll() throws Exception {
        given(this.bookingService.getAllByBooker(anyLong(), any(BookingState.class)))
                .willReturn(List.of(bookingDto(BookingStatus.WAITING)));

        this.mockMvc.perform(get("/bookings").header(ShareItHeaders.USER_ID, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(this.bookingService).getAllByBooker(2L, BookingState.ALL);
    }

    @Test
    void getAllByBooker_withUnknownState_returns400() throws Exception {
        this.mockMvc.perform(get("/bookings")
                        .header(ShareItHeaders.USER_ID, 2L)
                        .param("state", "UNSUPPORTED_STATUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unknown state: UNSUPPORTED_STATUS"));
    }

    @Test
    void getAllByOwner_passesParsedState() throws Exception {
        given(this.bookingService.getAllByOwner(anyLong(), any(BookingState.class)))
                .willReturn(List.of());

        this.mockMvc.perform(get("/bookings/owner")
                        .header(ShareItHeaders.USER_ID, 1L)
                        .param("state", "rejected"))
                .andExpect(status().isOk());

        verify(this.bookingService).getAllByOwner(1L, BookingState.REJECTED);
    }
}
