package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(Long ownerId, NewItemRequest request) {
        this.ensureUserExists(ownerId);
        Item saved = this.itemRepository.save(ItemMapper.toItem(request, ownerId));
        log.info("Создана вещь id={} владельца id={}", saved.getId(), ownerId);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, UpdateItemRequest request) {
        this.ensureUserExists(userId);
        Item existing = this.findItemOrThrow(itemId);
        if (!existing.getOwnerId().equals(userId)) {
            throw new ForbiddenException(
                    "Пользователь с id=%d не является владельцем вещи с id=%d".formatted(userId, itemId));
        }
        ItemMapper.updateItemFields(existing, request);
        Item saved = this.itemRepository.save(existing);
        log.info("Обновлена вещь id={}", saved.getId());
        return ItemMapper.toItemDto(saved);
    }

    @Override
    public ItemDto getById(Long itemId) {
        return ItemMapper.toItemDto(this.findItemOrThrow(itemId));
    }

    @Override
    public List<ItemDto> getAllByOwner(Long ownerId) {
        this.ensureUserExists(ownerId);
        return this.itemRepository.findAllByOwnerId(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return this.itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    private Item findItemOrThrow(Long itemId) {
        return this.itemRepository.findById(itemId)
                .orElseThrow(() -> NotFoundException.item(itemId));
    }

    private void ensureUserExists(Long userId) {
        if (this.userRepository.findById(userId).isEmpty()) {
            throw NotFoundException.user(userId);
        }
    }
}
