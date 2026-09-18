package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Проверка «дата не в прошлом» здесь отсутствует намеренно: она зависит от текущего
 * момента и живёт в {@code BookingServiceImpl} с допуском на задержку запроса.
 * Здесь остаются только детерминированные правила.
 */
@Data
public class NewBookingDto {

    @NotNull(message = "идентификатор вещи обязателен")
    private Long itemId;

    @NotNull(message = "дата начала обязательна")
    private LocalDateTime start;

    @NotNull(message = "дата окончания обязательна")
    private LocalDateTime end;

    @AssertTrue(message = "дата начала должна быть раньше даты окончания")
    @JsonIgnore
    public boolean isStartBeforeEnd() {
        return this.start == null || this.end == null || this.start.isBefore(this.end);
    }
}
