package ru.practicum.shareit.item.dto;

import lombok.Data;

/**
 * Ограничений пока нет намеренно. Класс и {@code @Valid} на нём сохраняются, чтобы при
 * появлении первого ограничения валидация запустилась сама, а не осталась незамеченной.
 */
@Data
public class UpdateItemDto {

    private String name;
    private String description;
    private Boolean available;
}
