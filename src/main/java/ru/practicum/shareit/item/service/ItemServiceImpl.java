package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.dto.NewCommentRequest;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto create(Long ownerId, NewItemRequest request) {
        User owner = this.findUserOrThrow(ownerId);
        Item saved = this.itemRepository.save(ItemMapper.toItem(request, owner));
        log.info("Создана вещь id={} владельца id={}", saved.getId(), ownerId);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, UpdateItemRequest request) {
        this.findUserOrThrow(userId);
        Item existing = this.findItemOrThrow(itemId);
        if (!existing.getOwner().getId().equals(userId)) {
            throw new ForbiddenException(
                    "Пользователь с id=%d не является владельцем вещи с id=%d".formatted(userId, itemId));
        }
        ItemMapper.updateItemFields(existing, request);
        Item saved = this.itemRepository.save(existing);
        log.info("Обновлена вещь id={}", saved.getId());
        return ItemMapper.toItemDto(saved);
    }

    @Override
    public ItemWithBookingsDto getById(Long userId, Long itemId) {
        Item item = this.findItemOrThrow(itemId);
        List<CommentDto> comments = this.commentRepository
                .findAllByItemIdOrderByCreatedDesc(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .toList();
        if (!item.getOwner().getId().equals(userId)) {
            return ItemMapper.toItemWithBookingsDto(item, null, null, comments);
        }
        List<Booking> bookings = this.bookingRepository
                .findAllByItemIdAndStatusOrderByStart(itemId, BookingStatus.APPROVED);
        LocalDateTime now = LocalDateTime.now();
        return ItemMapper.toItemWithBookingsDto(item, this.lastBooking(bookings, now),
                this.nextBooking(bookings, now), comments);
    }

    @Override
    public List<ItemWithBookingsDto> getAllByOwner(Long ownerId) {
        this.findUserOrThrow(ownerId);
        List<Item> items = this.itemRepository.findAllByOwnerIdOrderById(ownerId);
        List<Long> itemIds = items.stream().map(Item::getId).toList();
        Map<Long, List<Booking>> bookingsByItem = this.bookingRepository
                .findAllByItemIdInAndStatusOrderByStart(itemIds, BookingStatus.APPROVED).stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));
        Map<Long, List<CommentDto>> commentsByItem = this.commentRepository
                .findAllByItemIdInOrderByCreatedDesc(itemIds).stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId(),
                        Collectors.mapping(CommentMapper::toCommentDto, Collectors.toList())));
        LocalDateTime now = LocalDateTime.now();
        return items.stream()
                .map(item -> {
                    List<Booking> bookings = bookingsByItem.getOrDefault(item.getId(), List.of());
                    return ItemMapper.toItemWithBookingsDto(item,
                            this.lastBooking(bookings, now),
                            this.nextBooking(bookings, now),
                            commentsByItem.getOrDefault(item.getId(), List.of()));
                })
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

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, NewCommentRequest request) {
        User author = this.findUserOrThrow(userId);
        Item item = this.findItemOrThrow(itemId);
        boolean hasFinishedBooking = this.bookingRepository
                .existsByBookerIdAndItemIdAndStatusAndEndBefore(
                        userId, itemId, BookingStatus.APPROVED, LocalDateTime.now());
        if (!hasFinishedBooking) {
            throw new ValidationException(
                    "Пользователь с id=%d не брал вещь с id=%d в аренду или аренда ещё не завершена"
                            .formatted(userId, itemId));
        }
        Comment saved = this.commentRepository.save(Comment.builder()
                .text(request.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build());
        log.info("Добавлен комментарий id={} к вещи id={}", saved.getId(), itemId);
        return CommentMapper.toCommentDto(saved);
    }

    private Item findItemOrThrow(Long itemId) {
        return this.itemRepository.findById(itemId)
                .orElseThrow(() -> NotFoundException.item(itemId));
    }

    private User findUserOrThrow(Long userId) {
        return this.userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.user(userId));
    }

    private BookingShortDto lastBooking(List<Booking> bookings, LocalDateTime now) {
        return bookings.stream()
                .filter(booking -> !booking.getStart().isAfter(now))
                .max(Comparator.comparing(Booking::getStart))
                .map(BookingMapper::toBookingShortDto)
                .orElse(null);
    }

    private BookingShortDto nextBooking(List<Booking> bookings, LocalDateTime now) {
        return bookings.stream()
                .filter(booking -> booking.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart))
                .map(BookingMapper::toBookingShortDto)
                .orElse(null);
    }
}
