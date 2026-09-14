package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    /**
     * Допуск при сравнении присланных дат с текущим моментом. Клиент вычисляет даты до отправки
     * запроса, поэтому дата «через секунду» может дойти до сервера уже устаревшей; кроме того,
     * часы клиента и сервера расходятся. Допуск заведомо меньше любого осмысленного «прошлого».
     */
    private static final Duration CLOCK_TOLERANCE = Duration.ofMinutes(1);

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingDto create(Long userId, NewBookingRequest request) {
        User booker = this.findUserOrThrow(userId);
        Item item = this.findItemOrThrow(request.getItemId());
        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException(
                    "Вещь с id=%d недоступна для бронирования".formatted(item.getId()));
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException(
                    "Владелец не может забронировать собственную вещь с id=%d".formatted(item.getId()));
        }
        LocalDateTime earliestAllowed = LocalDateTime.now().minus(CLOCK_TOLERANCE);
        if (request.getStart().isBefore(earliestAllowed)) {
            throw new ValidationException("Дата начала бронирования не может быть в прошлом");
        }
        if (request.getEnd().isBefore(earliestAllowed)) {
            throw new ValidationException("Дата окончания бронирования не может быть в прошлом");
        }
        if (!request.getStart().isBefore(request.getEnd())) {
            throw new ValidationException("Дата начала бронирования должна быть раньше даты окончания");
        }
        Booking saved = this.bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(request.getStart())
                .end(request.getEnd())
                .status(BookingStatus.WAITING)
                .build());
        log.info("Создано бронирование id={} вещи id={}", saved.getId(), item.getId());
        return BookingMapper.toBookingDto(saved);
    }

    @Override
    @Transactional
    public BookingDto approve(Long userId, Long bookingId, boolean approved) {
        Booking booking = this.findBookingOrThrow(bookingId);
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException(
                    "Пользователь с id=%d не является владельцем вещи и не может изменить бронирование"
                            .formatted(userId));
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException(
                    "Бронирование с id=%d уже обработано".formatted(bookingId));
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking saved = this.bookingRepository.save(booking);
        log.info("Бронирование id={} переведено в статус {}", saved.getId(), saved.getStatus());
        return BookingMapper.toBookingDto(saved);
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = this.findBookingOrThrow(bookingId);
        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);
        if (!isBooker && !isOwner) {
            throw NotFoundException.booking(bookingId);
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllByBooker(Long userId, BookingState state) {
        this.findUserOrThrow(userId);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state) {
            case ALL -> this.bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
            case CURRENT -> this.bookingRepository
                    .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);
            case PAST -> this.bookingRepository
                    .findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE -> this.bookingRepository
                    .findAllByBookerIdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING -> this.bookingRepository
                    .findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED -> this.bookingRepository
                    .findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
        };
        return bookings.stream().map(BookingMapper::toBookingDto).toList();
    }

    @Override
    public List<BookingDto> getAllByOwner(Long userId, BookingState state) {
        this.findUserOrThrow(userId);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state) {
            case ALL -> this.bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId);
            case CURRENT -> this.bookingRepository
                    .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);
            case PAST -> this.bookingRepository
                    .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE -> this.bookingRepository
                    .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING -> this.bookingRepository
                    .findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED -> this.bookingRepository
                    .findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
        };
        return bookings.stream().map(BookingMapper::toBookingDto).toList();
    }

    private Booking findBookingOrThrow(Long bookingId) {
        return this.bookingRepository.findById(bookingId)
                .orElseThrow(() -> NotFoundException.booking(bookingId));
    }

    private Item findItemOrThrow(Long itemId) {
        return this.itemRepository.findById(itemId)
                .orElseThrow(() -> NotFoundException.item(itemId));
    }

    private User findUserOrThrow(Long userId) {
        return this.userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.user(userId));
    }
}
