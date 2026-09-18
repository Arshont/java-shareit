package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import java.util.List;

public interface ItemService {

    ItemDto create(Long ownerId, NewItemDto request);

    ItemDto update(Long userId, Long itemId, UpdateItemDto request);

    ItemWithBookingsDto getById(Long userId, Long itemId);

    List<ItemWithBookingsDto> getAllByOwner(Long ownerId);

    List<ItemDto> search(String text);

    CommentDto addComment(Long userId, Long itemId, NewCommentDto request);
}
