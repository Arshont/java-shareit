package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto create(Long userId, NewItemRequestDto request);

    List<ItemRequestWithItemsDto> getOwn(Long userId);

    List<ItemRequestDto> getAll(Long userId);

    ItemRequestWithItemsDto getById(Long userId, Long requestId);
}
