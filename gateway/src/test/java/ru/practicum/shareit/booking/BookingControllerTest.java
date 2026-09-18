package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.common.ShareItHeaders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String VALID_BODY =
            "{\"itemId\":1,\"start\":\"2030-01-01T10:00:00\",\"end\":\"2030-01-02T10:00:00\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void create_withValidBody_delegatesToClient() throws Exception {
        given(this.bookingClient.create(anyLong(), any(NewBookingDto.class)))
                .willReturn(ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", 1)));

        this.mockMvc.perform(post("/bookings")
                        .header(ShareItHeaders.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_withoutStart_returns400AndDoesNotCallClient() throws Exception {
        this.mockMvc.perform(post("/bookings")
                        .header(ShareItHeaders.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":1,\"end\":\"2030-01-02T10:00:00\"}"))
                .andExpect(status().isBadRequest());

        verify(this.bookingClient, never()).create(anyLong(), any());
    }

    @Test
    void create_withoutEnd_returns400AndDoesNotCallClient() throws Exception {
        this.mockMvc.perform(post("/bookings")
                        .header(ShareItHeaders.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":1,\"start\":\"2030-01-01T10:00:00\"}"))
                .andExpect(status().isBadRequest());

        verify(this.bookingClient, never()).create(anyLong(), any());
    }

    @Test
    void create_withStartEqualToEnd_returns400AndDoesNotCallClient() throws Exception {
        this.mockMvc.perform(post("/bookings")
                        .header(ShareItHeaders.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":1,\"start\":\"2030-01-01T10:00:00\","
                                + "\"end\":\"2030-01-01T10:00:00\"}"))
                .andExpect(status().isBadRequest());

        verify(this.bookingClient, never()).create(anyLong(), any());
    }

    @Test
    void create_withStartInPast_isPassedThroughToServer() throws Exception {
        given(this.bookingClient.create(anyLong(), any(NewBookingDto.class)))
                .willReturn(ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", 1)));

        this.mockMvc.perform(post("/bookings")
                        .header(ShareItHeaders.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":1,\"start\":\"2020-01-01T10:00:00\","
                                + "\"end\":\"2020-01-02T10:00:00\"}"))
                .andExpect(status().isCreated());

        verify(this.bookingClient).create(anyLong(), any(NewBookingDto.class));
    }

    @Test
    void getAllByBooker_withUnknownState_returns400AndDoesNotCallClient() throws Exception {
        this.mockMvc.perform(get("/bookings")
                        .header(ShareItHeaders.USER_ID, 1L)
                        .param("state", "UNSUPPORTED_STATUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unknown state: UNSUPPORTED_STATUS"));

        verify(this.bookingClient, never()).getAllByBooker(anyLong(), any());
    }

    @Test
    void getAllByBooker_withoutState_usesAll() throws Exception {
        given(this.bookingClient.getAllByBooker(anyLong(), any(BookingState.class)))
                .willReturn(ResponseEntity.ok(java.util.List.of()));

        this.mockMvc.perform(get("/bookings").header(ShareItHeaders.USER_ID, 1L))
                .andExpect(status().isOk());

        verify(this.bookingClient).getAllByBooker(1L, BookingState.ALL);
    }

    @Test
    void approve_whenServerRejects_propagatesStatusAndJsonBody() throws Exception {
        given(this.bookingClient.approve(anyLong(), anyLong(), org.mockito.ArgumentMatchers.anyBoolean()))
                .willThrow(HttpClientErrorException.create(HttpStatus.FORBIDDEN, "Forbidden",
                        org.springframework.http.HttpHeaders.EMPTY,
                        "{\"error\":\"нет прав\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        java.nio.charset.StandardCharsets.UTF_8));

        this.mockMvc.perform(patch("/bookings/1")
                        .header(ShareItHeaders.USER_ID, 1L)
                        .param("approved", "true"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("нет прав"));
    }

    @Test
    void approve_withoutApprovedParam_returns400AndDoesNotCallClient() throws Exception {
        this.mockMvc.perform(patch("/bookings/1")
                        .header(ShareItHeaders.USER_ID, 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(this.bookingClient, never()).approve(anyLong(), anyLong(), anyBoolean());
    }
}
