package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.common.ShareItHeaders;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(ShareItHeaders.USER_ID) long userId,
                                         @Valid @RequestBody NewBookingDto request) {
        log.info("Создание бронирования {} пользователем id={}", request, userId);
        return this.bookingClient.create(userId, request);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approve(@RequestHeader(ShareItHeaders.USER_ID) long userId,
                                          @PathVariable long bookingId,
                                          @RequestParam boolean approved) {
        log.info("Подтверждение бронирования id={} значением {}", bookingId, approved);
        return this.bookingClient.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getById(@RequestHeader(ShareItHeaders.USER_ID) long userId,
                                          @PathVariable long bookingId) {
        log.info("Бронирование id={} для пользователя id={}", bookingId, userId);
        return this.bookingClient.getById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByBooker(@RequestHeader(ShareItHeaders.USER_ID) long userId,
                                                 @RequestParam(defaultValue = "ALL") String state) {
        log.info("Бронирования арендатора id={} со статусом {}", userId, state);
        return this.bookingClient.getAllByBooker(userId, BookingState.from(state));
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllByOwner(@RequestHeader(ShareItHeaders.USER_ID) long userId,
                                                @RequestParam(defaultValue = "ALL") String state) {
        log.info("Бронирования владельца id={} со статусом {}", userId, state);
        return this.bookingClient.getAllByOwner(userId, BookingState.from(state));
    }
}
