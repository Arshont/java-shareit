package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemMapper {

    public static Item toItem(NewItemRequest request, User owner) {
        return Item.builder()
                .name(request.getName())
                .description(request.getDescription())
                .available(request.getAvailable())
                .owner(owner)
                .requestId(request.getRequestId())
                .build();
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

    public static ItemWithBookingsDto toItemWithBookingsDto(Item item, BookingShortDto lastBooking,
                                                            BookingShortDto nextBooking,
                                                            List<CommentDto> comments) {
        return new ItemWithBookingsDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequestId(),
                lastBooking,
                nextBooking,
                comments
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
