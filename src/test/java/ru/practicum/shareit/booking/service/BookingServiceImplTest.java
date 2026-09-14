package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.AbstractIntegrationTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingServiceImplTest extends AbstractIntegrationTest {

    @Autowired
    private BookingService bookingService;

    private User owner;
    private User booker;
    private User stranger;
    private Item item;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUpFixture() {
        this.owner = this.createUser("Владелец", "owner@example.com");
        this.booker = this.createUser("Арендатор", "booker@example.com");
        this.stranger = this.createUser("Посторонний", "stranger@example.com");
        this.item = this.createItem("Дрель", "Ударная", true, this.owner);
        this.start = LocalDateTime.now().plusDays(1).withNano(0);
        this.end = LocalDateTime.now().plusDays(2).withNano(0);
    }

    @Test
    void create_returnsWaitingBookingWithNestedFields() {
        BookingDto created = this.bookingService.create(this.booker.getId(), this.newBooking(this.item.getId()));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getStart()).isEqualTo(this.start);
        assertThat(created.getEnd()).isEqualTo(this.end);
        assertThat(created.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(created.getBooker().getId()).isEqualTo(this.booker.getId());
        assertThat(created.getItem().getId()).isEqualTo(this.item.getId());
        assertThat(created.getItem().getName()).isEqualTo("Дрель");
    }

    @Test
    void create_withUnavailableItem_throwsValidation() {
        Item unavailable = this.createItem("Пила", "Циркулярная", false, this.owner);

        assertThatThrownBy(() -> this.bookingService.create(this.booker.getId(),
                this.newBooking(unavailable.getId())))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_withUnknownUser_throwsNotFound() {
        assertThatThrownBy(() -> this.bookingService.create(9999L, this.newBooking(this.item.getId())))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_withUnknownItem_throwsNotFound() {
        assertThatThrownBy(() -> this.bookingService.create(this.booker.getId(), this.newBooking(9999L)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_byOwnerOfItem_throwsNotFound() {
        assertThatThrownBy(() -> this.bookingService.create(this.owner.getId(),
                this.newBooking(this.item.getId())))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_withStartEqualToEnd_throwsValidation() {
        NewBookingRequest request = new NewBookingRequest();
        request.setItemId(this.item.getId());
        request.setStart(this.start);
        request.setEnd(this.start);

        assertThatThrownBy(() -> this.bookingService.create(this.booker.getId(), request))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_withStartAfterEnd_throwsValidation() {
        NewBookingRequest request = new NewBookingRequest();
        request.setItemId(this.item.getId());
        request.setStart(this.end);
        request.setEnd(this.start);

        assertThatThrownBy(() -> this.bookingService.create(this.booker.getId(), request))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_withStartLongInPast_throwsValidation() {
        NewBookingRequest request = new NewBookingRequest();
        request.setItemId(this.item.getId());
        request.setStart(LocalDateTime.now().minusDays(1).withNano(0));
        request.setEnd(this.end);

        assertThatThrownBy(() -> this.bookingService.create(this.booker.getId(), request))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_withEndLongInPast_throwsValidation() {
        NewBookingRequest request = new NewBookingRequest();
        request.setItemId(this.item.getId());
        request.setStart(LocalDateTime.now().minusDays(3).withNano(0));
        request.setEnd(LocalDateTime.now().minusDays(2).withNano(0));

        assertThatThrownBy(() -> this.bookingService.create(this.booker.getId(), request))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_withStartJustMissedByRequestLatency_succeeds() {
        NewBookingRequest request = new NewBookingRequest();
        request.setItemId(this.item.getId());
        request.setStart(LocalDateTime.now().minusSeconds(5).withNano(0));
        request.setEnd(LocalDateTime.now().plusHours(1).withNano(0));

        BookingDto created = this.bookingService.create(this.booker.getId(), request);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void approve_byOwner_setsApproved() {
        Booking booking = this.createBooking(this.item, this.booker, this.start, this.end,
                BookingStatus.WAITING);

        BookingDto approved = this.bookingService.approve(this.owner.getId(), booking.getId(), true);

        assertThat(approved.getId()).isEqualTo(booking.getId());
        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approve_withFalse_setsRejected() {
        Booking booking = this.createBooking(this.item, this.booker, this.start, this.end,
                BookingStatus.WAITING);

        BookingDto rejected = this.bookingService.approve(this.owner.getId(), booking.getId(), false);

        assertThat(rejected.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void approve_byNonOwner_throwsForbidden() {
        Booking booking = this.createBooking(this.item, this.booker, this.start, this.end,
                BookingStatus.WAITING);

        assertThatThrownBy(() -> this.bookingService.approve(this.stranger.getId(), booking.getId(), true))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void approve_alreadyProcessed_throwsValidation() {
        Booking booking = this.createBooking(this.item, this.booker, this.start, this.end,
                BookingStatus.APPROVED);

        assertThatThrownBy(() -> this.bookingService.approve(this.owner.getId(), booking.getId(), true))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void approve_whenBookingAbsent_throwsNotFound() {
        assertThatThrownBy(() -> this.bookingService.approve(this.owner.getId(), 9999L, true))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_byBooker_returnsBooking() {
        Booking booking = this.createBooking(this.item, this.booker, this.start, this.end,
                BookingStatus.WAITING);

        BookingDto found = this.bookingService.getById(this.booker.getId(), booking.getId());

        assertThat(found.getId()).isEqualTo(booking.getId());
        assertThat(found.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void getById_byOwner_returnsBooking() {
        Booking booking = this.createBooking(this.item, this.booker, this.start, this.end,
                BookingStatus.WAITING);

        BookingDto found = this.bookingService.getById(this.owner.getId(), booking.getId());

        assertThat(found.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getById_byThirdParty_throwsNotFound() {
        Booking booking = this.createBooking(this.item, this.booker, this.start, this.end,
                BookingStatus.WAITING);

        assertThatThrownBy(() -> this.bookingService.getById(this.stranger.getId(), booking.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_whenBookingAbsent_throwsNotFound() {
        assertThatThrownBy(() -> this.bookingService.getById(this.booker.getId(), 9999L))
                .isInstanceOf(NotFoundException.class);
    }

    private NewBookingRequest newBooking(Long itemId) {
        NewBookingRequest request = new NewBookingRequest();
        request.setItemId(itemId);
        request.setStart(this.start);
        request.setEnd(this.end);
        return request;
    }
}
