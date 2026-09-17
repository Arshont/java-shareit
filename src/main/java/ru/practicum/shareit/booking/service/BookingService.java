package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.NewBookingRequest;

import java.util.List;

public interface BookingService {

    BookingDto create(Long userId, NewBookingRequest request);

    BookingDto approve(Long userId, Long bookingId, boolean approved);

    BookingDto getById(Long userId, Long bookingId);

    List<BookingDto> getAllByBooker(Long userId, BookingState state);

    List<BookingDto> getAllByOwner(Long userId, BookingState state);
}
