package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.model.Item;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemMapper {

    public static Item toItem(NewItemRequest request, Long ownerId) {
        Item item = new Item();
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setAvailable(request.getAvailable());
        item.setOwnerId(ownerId);
        item.setRequestId(request.getRequestId());
        return item;
    }

    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequestId()
        );
    }

    public static void updateItemFields(Item existing, UpdateItemRequest request) {
        if (request.hasName()) {
            existing.setName(request.getName());
        }
        if (request.hasDescription()) {
            existing.setDescription(request.getDescription());
        }
        if (request.hasAvailable()) {
            existing.setAvailable(request.getAvailable());
        }
    }
}
