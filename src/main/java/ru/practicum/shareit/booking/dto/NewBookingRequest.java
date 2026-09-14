package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Проверка дат на «не в прошлом» живёт в {@code BookingServiceImpl}, а не здесь:
 * {@code @FutureOrPresent} сравнивает с текущим моментом без допуска, а клиент формирует
 * даты до отправки запроса, поэтому близкая к «сейчас» дата успевает устареть в пути.
 */
@Data
public class NewBookingRequest {

    @NotNull(message = "идентификатор вещи обязателен")
    private Long itemId;

    @NotNull(message = "дата начала обязательна")
    private LocalDateTime start;

    @NotNull(message = "дата окончания обязательна")
    private LocalDateTime end;
}
