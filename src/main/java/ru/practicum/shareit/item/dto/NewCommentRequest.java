package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewCommentRequest {

    @NotBlank(message = "текст комментария не может быть пустым")
    private String text;
}
