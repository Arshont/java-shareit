package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.common.ShareItHeaders;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/requests")
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(ShareItHeaders.USER_ID) long userId,
                                         @Valid @RequestBody NewItemRequestDto request) {
        log.info("Создание запроса вещи {} пользователем id={}", request, userId);
        return this.itemRequestClient.create(userId, request);
    }

    @GetMapping
    public ResponseEntity<Object> getOwn(@RequestHeader(ShareItHeaders.USER_ID) long userId) {
        log.info("Запросы пользователя id={}", userId);
        return this.itemRequestClient.getOwn(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@RequestHeader(ShareItHeaders.USER_ID) long userId) {
        log.info("Чужие запросы для пользователя id={}", userId);
        return this.itemRequestClient.getAll(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@RequestHeader(ShareItHeaders.USER_ID) long userId,
                                          @PathVariable long requestId) {
        log.info("Запрос id={} для пользователя id={}", requestId, userId);
        return this.itemRequestClient.getById(userId, requestId);
    }
}
