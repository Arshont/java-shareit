package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewBookingRequest {

    @NotNull(message = "идентификатор вещи обязателен")
    private Long itemId;

    @NotNull(message = "дата начала обязательна")
    @FutureOrPresent(message = "дата начала не может быть в прошлом")
    private LocalDateTime start;

    @NotNull(message = "дата окончания обязательна")
    @Future(message = "дата окончания должна быть в будущем")
    private LocalDateTime end;
}
