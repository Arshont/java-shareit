package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.common.ShareItHeaders;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto create(@RequestHeader(ShareItHeaders.USER_ID) Long userId,
                                 @RequestBody NewItemRequestDto request) {
        return this.itemRequestService.create(userId, request);
    }

    @GetMapping
    public List<ItemRequestWithItemsDto> getOwn(@RequestHeader(ShareItHeaders.USER_ID) Long userId) {
        return this.itemRequestService.getOwn(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAll(@RequestHeader(ShareItHeaders.USER_ID) Long userId) {
        return this.itemRequestService.getAll(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestWithItemsDto getById(@RequestHeader(ShareItHeaders.USER_ID) Long userId,
                                           @PathVariable Long requestId) {
        return this.itemRequestService.getById(userId, requestId);
    }
}
