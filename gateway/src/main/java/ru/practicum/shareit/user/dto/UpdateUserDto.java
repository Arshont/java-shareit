package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserDto {

    private String name;

    @Email(message = "почта должна быть корректной")
    private String email;
}
