package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.model.Item;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    @Test
    void toItem_copiesFieldsAndSetsOwner() {
        NewItemRequest request = new NewItemRequest();
        request.setName("Дрель");
        request.setDescription("Ударная, 600 Вт");
        request.setAvailable(true);
        request.setRequestId(3L);

        Item item = ItemMapper.toItem(request, 5L);

        assertThat(item.getId()).isNull();
        assertThat(item.getName()).isEqualTo("Дрель");
        assertThat(item.getDescription()).isEqualTo("Ударная, 600 Вт");
        assertThat(item.getAvailable()).isTrue();
        assertThat(item.getOwnerId()).isEqualTo(5L);
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
    void updateItemFields_doesNotChangeOwner() {
        Item existing = this.existingItem();
        UpdateItemRequest request = new UpdateItemRequest();
        request.setName("Перфоратор");

        ItemMapper.updateItemFields(existing, request);

        assertThat(existing.getOwnerId()).isEqualTo(5L);
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

    private Item existingItem() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Ударная, 600 Вт");
        item.setAvailable(true);
        item.setOwnerId(5L);
        item.setRequestId(3L);
        return item;
    }
}
