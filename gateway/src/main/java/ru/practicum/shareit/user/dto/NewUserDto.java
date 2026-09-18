package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewUserDto {

    @NotBlank(message = "имя не может быть пустым")
    private String name;

    @NotBlank(message = "почта не может быть пустой")
    @Email(message = "почта должна быть корректной")
    private String email;
}
