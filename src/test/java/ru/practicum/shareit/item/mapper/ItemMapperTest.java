package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    @Test
    void toItem_copiesFieldsAndSetsOwner() {
        NewItemRequest request = new NewItemRequest();
        request.setName("Дрель");
        request.setDescription("Ударная, 600 Вт");
        request.setAvailable(true);
        request.setRequestId(3L);
        User owner = User.builder().id(5L).name("Владелец").email("owner@example.com").build();

        Item item = ItemMapper.toItem(request, owner);

        assertThat(item.getId()).isNull();
        assertThat(item.getName()).isEqualTo("Дрель");
        assertThat(item.getDescription()).isEqualTo("Ударная, 600 Вт");
        assertThat(item.getAvailable()).isTrue();
        assertThat(item.getOwner()).isSameAs(owner);
        assertThat(item.getRequestId()).isEqualTo(3L);
    }

    @Test
    void toItemDto_copiesAllFieldsExceptOwner() {
        Item item = this.existingItem();

        ItemDto dto = ItemMapper.toItemDto(item);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getDescription()).isEqualTo("Ударная, 600 Вт");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isEqualTo(3L);
    }

    @Test
    void updateItemFields_withOnlyAvailable_keepsNameAndDescription() {
        Item existing = this.existingItem();
        UpdateItemRequest request = new UpdateItemRequest();
        request.setAvailable(false);

        ItemMapper.updateItemFields(existing, request);

        assertThat(existing.getAvailable()).isFalse();
        assertThat(existing.getName()).isEqualTo("Дрель");
        assertThat(existing.getDescription()).isEqualTo("Ударная, 600 Вт");
    }

    @Test
    void updateItemFields_withOnlyName_keepsDescriptionAndAvailable() {
        Item existing = this.existingItem();
        UpdateItemRequest request = new UpdateItemRequest();
        request.setName("Перфоратор");

        ItemMapper.updateItemFields(existing, request);

        assertThat(existing.getName()).isEqualTo("Перфоратор");
        assertThat(existing.getDescription()).isEqualTo("Ударная, 600 Вт");
        assertThat(existing.getAvailable()).isTrue();
    }

    @Test
    void updateItemFields_withOnlyDescription_keepsNameAndAvailable() {
        Item existing = this.existingItem();
        UpdateItemRequest request = new UpdateItemRequest();
        request.setDescription("Новое описание");

        ItemMapper.updateItemFields(existing, request);

        assertThat(existing.getName()).isEqualTo("Дрель");
        assertThat(existing.getDescription()).isEqualTo("Новое описание");
        assertThat(existing.getAvailable()).isTrue();
    }

    @Test
    void updateItemFields_withBlankValues_keepsNameAndDescription() {
        Item existing = this.existingItem();
        UpdateItemRequest request = new UpdateItemRequest();
        request.setName("   ");
        request.setDescription("");

        ItemMapper.updateItemFields(existing, request);

        assertThat(existing.getName()).isEqualTo("Дрель");
        assertThat(existing.getDescription()).isEqualTo("Ударная, 600 Вт");
    }

    @Test
    void updateItemFields_doesNotChangeOwner() {
        Item existing = this.existingItem();
        User ownerBefore = existing.getOwner();
        UpdateItemRequest request = new UpdateItemRequest();
        request.setName("Перфоратор");

        ItemMapper.updateItemFields(existing, request);

        assertThat(existing.getOwner()).isSameAs(ownerBefore);
    }

    @Test
    void toItemWithBookingsDto_copiesFieldsAndAttachesBookingsAndComments() {
        Item item = this.existingItem();
        BookingShortDto last = new BookingShortDto(10L, 2L);
        BookingShortDto next = new BookingShortDto(11L, 3L);
        List<CommentDto> comments = List.of(
                new CommentDto(5L, "Отличная дрель", "Иван", LocalDateTime.of(2026, 10, 1, 12, 0, 0)));

        ItemWithBookingsDto dto = ItemMapper.toItemWithBookingsDto(item, last, next, comments);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getDescription()).isEqualTo("Ударная, 600 Вт");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isEqualTo(3L);
        assertThat(dto.getLastBooking()).isSameAs(last);
        assertThat(dto.getNextBooking()).isSameAs(next);
        assertThat(dto.getComments()).containsExactlyElementsOf(comments);
    }

    private Item existingItem() {
        User owner = User.builder().id(5L).name("Владелец").email("owner@example.com").build();
        return Item.builder()
                .id(1L)
                .name("Дрель")
                .description("Ударная, 600 Вт")
                .available(true)
                .owner(owner)
                .requestId(3L)
                .build();
    }
}
