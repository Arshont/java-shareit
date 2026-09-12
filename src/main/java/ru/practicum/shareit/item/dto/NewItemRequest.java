package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NewItemRequest {

    @NotBlank(message = "название не может быть пустым")
    private String name;

    @NotBlank(message = "описание не может быть пустым")
    private String description;

    @NotNull(message = "статус доступности обязателен")
    private Boolean available;

    private Long requestId;
}
