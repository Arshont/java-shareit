package ru.practicum.shareit.booking.controller;

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
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.common.ShareItHeaders;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto create(@RequestHeader(ShareItHeaders.USER_ID) Long userId,
                             @Valid @RequestBody NewBookingRequest request) {
        return this.bookingService.create(userId, request);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader(ShareItHeaders.USER_ID) Long userId,
                              @PathVariable Long bookingId,
                              @RequestParam boolean approved) {
        return this.bookingService.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(@RequestHeader(ShareItHeaders.USER_ID) Long userId,
                              @PathVariable Long bookingId) {
        return this.bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getAllByBooker(@RequestHeader(ShareItHeaders.USER_ID) Long userId,
                                           @RequestParam(defaultValue = "ALL") String state) {
        return this.bookingService.getAllByBooker(userId, BookingState.from(state));
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllByOwner(@RequestHeader(ShareItHeaders.USER_ID) Long userId,
                                          @RequestParam(defaultValue = "ALL") String state) {
        return this.bookingService.getAllByOwner(userId, BookingState.from(state));
    }
}
