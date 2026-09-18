package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.AbstractIntegrationTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingStateFilterTest extends AbstractIntegrationTest {

    @Autowired
    private BookingService bookingService;

    private User owner;
    private User booker;
    private Booking past;
    private Booking current;
    private Booking future;
    private Booking rejected;

    @BeforeEach
    void setUpFixture() {
        this.owner = this.createUser("Владелец", "owner@example.com");
        this.booker = this.createUser("Арендатор", "booker@example.com");
        Item item = this.createItem("Дрель", "Ударная", true, this.owner);
        LocalDateTime now = LocalDateTime.now();

        this.past = this.createBooking(item, this.booker,
                now.minusDays(5), now.minusDays(4), BookingStatus.APPROVED);
        this.current = this.createBooking(item, this.booker,
                now.minusDays(1), now.plusDays(1), BookingStatus.APPROVED);
        this.future = this.createBooking(item, this.booker,
                now.plusDays(4), now.plusDays(5), BookingStatus.WAITING);
        this.rejected = this.createBooking(item, this.booker,
                now.plusDays(8), now.plusDays(9), BookingStatus.REJECTED);
    }

    @Test
    void getAllByBooker_all_returnsEverythingNewestFirst() {
        assertThat(this.bookingService.getAllByBooker(this.booker.getId(), BookingState.ALL))
                .extracting(BookingDto::getId)
                .containsExactly(this.rejected.getId(), this.future.getId(),
                        this.current.getId(), this.past.getId());
    }

    @Test
    void getAllByBooker_past_returnsOnlyFinished() {
        assertThat(this.bookingService.getAllByBooker(this.booker.getId(), BookingState.PAST))
                .extracting(BookingDto::getId)
                .containsExactly(this.past.getId());
    }

    @Test
    void getAllByBooker_current_returnsOnlyOngoing() {
        assertThat(this.bookingService.getAllByBooker(this.booker.getId(), BookingState.CURRENT))
                .extracting(BookingDto::getId)
                .containsExactly(this.current.getId());
    }

    @Test
    void getAllByBooker_future_returnsOnlyUpcoming() {
        assertThat(this.bookingService.getAllByBooker(this.booker.getId(), BookingState.FUTURE))
                .extracting(BookingDto::getId)
                .containsExactly(this.rejected.getId(), this.future.getId());
    }

    @Test
    void getAllByBooker_waiting_filtersByStatus() {
        assertThat(this.bookingService.getAllByBooker(this.booker.getId(), BookingState.WAITING))
                .extracting(BookingDto::getId)
                .containsExactly(this.future.getId());
    }

    @Test
    void getAllByBooker_rejected_filtersByStatus() {
        assertThat(this.bookingService.getAllByBooker(this.booker.getId(), BookingState.REJECTED))
                .extracting(BookingDto::getId)
                .containsExactly(this.rejected.getId());
    }

    @Test
    void getAllByBooker_withUnknownUser_throwsNotFound() {
        assertThatThrownBy(() -> this.bookingService.getAllByBooker(9999L, BookingState.ALL))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllByOwner_all_returnsEverythingNewestFirst() {
        assertThat(this.bookingService.getAllByOwner(this.owner.getId(), BookingState.ALL))
                .extracting(BookingDto::getId)
                .containsExactly(this.rejected.getId(), this.future.getId(),
                        this.current.getId(), this.past.getId());
    }

    @Test
    void getAllByOwner_past_returnsOnlyFinished() {
        assertThat(this.bookingService.getAllByOwner(this.owner.getId(), BookingState.PAST))
                .extracting(BookingDto::getId)
                .containsExactly(this.past.getId());
    }

    @Test
    void getAllByOwner_waiting_filtersByStatus() {
        assertThat(this.bookingService.getAllByOwner(this.owner.getId(), BookingState.WAITING))
                .extracting(BookingDto::getId)
                .containsExactly(this.future.getId());
    }

    @Test
    void getAllByOwner_forUserWithoutItems_returnsEmptyList() {
        User outsider = this.createUser("Без вещей", "outsider@example.com");

        assertThat(this.bookingService.getAllByOwner(outsider.getId(), BookingState.ALL)).isEmpty();
    }

    @Test
    void getAllByOwner_withUnknownUser_throwsNotFound() {
        assertThatThrownBy(() -> this.bookingService.getAllByOwner(9999L, BookingState.ALL))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllByOwner_current_returnsOnlyOngoing() {
        assertThat(this.bookingService.getAllByOwner(this.owner.getId(), BookingState.CURRENT))
                .extracting(BookingDto::getId)
                .containsExactly(this.current.getId());
    }

    @Test
    void getAllByOwner_future_returnsOnlyUpcoming() {
        assertThat(this.bookingService.getAllByOwner(this.owner.getId(), BookingState.FUTURE))
                .extracting(BookingDto::getId)
                .containsExactly(this.rejected.getId(), this.future.getId());
    }

    @Test
    void getAllByOwner_rejected_filtersByStatus() {
        assertThat(this.bookingService.getAllByOwner(this.owner.getId(), BookingState.REJECTED))
                .extracting(BookingDto::getId)
                .containsExactly(this.rejected.getId());
    }
}
