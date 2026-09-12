package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequest {

    private String name;

    @Email(message = "email должен быть корректным адресом")
    private String email;

    public boolean hasName() {
        return this.name != null && !this.name.isBlank();
    }

    public boolean hasEmail() {
        return this.email != null && !this.email.isBlank();
    }
}
