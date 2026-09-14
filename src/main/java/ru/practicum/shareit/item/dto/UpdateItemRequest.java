package ru.practicum.shareit.item.dto;

import lombok.Data;

@Data
public class UpdateItemRequest {

    private String name;
    private String description;
    private Boolean available;

    public boolean hasName() {
        return this.name != null && !this.name.isBlank();
    }

    public boolean hasDescription() {
        return this.description != null && !this.description.isBlank();
    }

    public boolean hasAvailable() {
        return this.available != null;
    }
}
