package ru.practicum.shareit.booking.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Проверка дат на «не в прошлом» живёт в {@code BookingServiceImpl}, а не здесь:
 * сравнение с текущим моментом без допуска на задержку запроса отвергает дату,
 * которую клиент формирует до отправки, поэтому близкая к «сейчас» дата успевает
 * устареть в пути.
 */
@Data
public class NewBookingDto {

    private Long itemId;

    private LocalDateTime start;

    private LocalDateTime end;
}
