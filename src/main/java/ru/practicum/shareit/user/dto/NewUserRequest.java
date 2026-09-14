package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewUserRequest {

    @NotBlank(message = "имя не может быть пустым")
    private String name;

    @NotBlank(message = "email не может быть пустым")
    @Email(message = "email должен быть корректным адресом")
    private String email;
}
