package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.common.ShareItHeaders;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(ShareItHeaders.USER_ID) long userId,
                                         @Valid @RequestBody NewItemDto request) {
        log.info("Создание вещи {} владельцем id={}", request, userId);
        return this.itemClient.create(userId, request);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(ShareItHeaders.USER_ID) long userId,
                                         @PathVariable long itemId,
                                         @Valid @RequestBody UpdateItemDto request) {
        log.info("Обновление вещи id={} пользователем id={}", itemId, userId);
        return this.itemClient.update(userId, itemId, request);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@RequestHeader(ShareItHeaders.USER_ID) long userId,
                                          @PathVariable long itemId) {
        log.info("Вещь id={} для пользователя id={}", itemId, userId);
        return this.itemClient.getById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByOwner(@RequestHeader(ShareItHeaders.USER_ID) long userId) {
        log.info("Вещи владельца id={}", userId);
        return this.itemClient.getAllByOwner(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader(ShareItHeaders.USER_ID) long userId,
                                         @RequestParam(defaultValue = "") String text) {
        log.info("Поиск вещей по тексту '{}'", text);
        return this.itemClient.search(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(ShareItHeaders.USER_ID) long userId,
                                             @PathVariable long itemId,
                                             @Valid @RequestBody NewCommentDto request) {
        log.info("Комментарий к вещи id={} от пользователя id={}", itemId, userId);
        return this.itemClient.addComment(userId, itemId, request);
    }
}
