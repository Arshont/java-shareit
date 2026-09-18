package ru.practicum.shareit.user.dto;

import lombok.Data;

@Data
public class UpdateUserDto {

    private String name;

    private String email;

    public boolean hasName() {
        return this.name != null && !this.name.isBlank();
    }

    public boolean hasEmail() {
        return this.email != null && !this.email.isBlank();
    }
}
