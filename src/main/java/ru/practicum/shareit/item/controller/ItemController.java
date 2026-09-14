package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@RequestHeader(USER_ID_HEADER) Long userId,
                          @Valid @RequestBody NewItemRequest request) {
        return this.itemService.create(userId, request);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_ID_HEADER) Long userId,
                          @PathVariable Long itemId,
                          @Valid @RequestBody UpdateItemRequest request) {
        return this.itemService.update(userId, itemId, request);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@RequestHeader(USER_ID_HEADER) Long userId, @PathVariable Long itemId) {
        return this.itemService.getById(itemId);
    }

    @GetMapping
    public List<ItemDto> getAllByOwner(@RequestHeader(USER_ID_HEADER) Long userId) {
        return this.itemService.getAllByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestHeader(USER_ID_HEADER) Long userId,
                                @RequestParam(defaultValue = "") String text) {
        return this.itemService.search(text);
    }
}
