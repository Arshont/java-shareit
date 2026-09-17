package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.common.ShareItHeaders;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.dto.NewCommentRequest;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@RequestHeader(ShareItHeaders.USER_ID) Long userId,
                          @Valid @RequestBody NewItemRequest request) {
        return this.itemService.create(userId, request);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(ShareItHeaders.USER_ID) Long userId,
                          @PathVariable Long itemId,
                          @Valid @RequestBody UpdateItemRequest request) {
        return this.itemService.update(userId, itemId, request);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingsDto getById(@RequestHeader(ShareItHeaders.USER_ID) Long userId,
                                       @PathVariable Long itemId) {
        return this.itemService.getById(userId, itemId);
    }

    @GetMapping
    public List<ItemWithBookingsDto> getAllByOwner(@RequestHeader(ShareItHeaders.USER_ID) Long userId) {
        return this.itemService.getAllByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestHeader(ShareItHeaders.USER_ID) Long userId,
                                @RequestParam(defaultValue = "") String text) {
        return this.itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@RequestHeader(ShareItHeaders.USER_ID) Long userId,
                                 @PathVariable Long itemId,
                                 @Valid @RequestBody NewCommentRequest request) {
        return this.itemService.addComment(userId, itemId, request);
    }
}
