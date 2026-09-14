package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;

import java.util.List;

public interface ItemService {

    ItemDto create(Long ownerId, NewItemRequest request);

    ItemDto update(Long userId, Long itemId, UpdateItemRequest request);

    ItemDto getById(Long itemId);

    List<ItemDto> getAllByOwner(Long ownerId);

    List<ItemDto> search(String text);
}
