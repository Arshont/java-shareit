package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.AbstractIntegrationTest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemBookingsTest extends AbstractIntegrationTest {

    @Autowired
    private ItemService itemService;

    private User owner;
    private User booker;
    private Item item;
    private LocalDateTime now;

    @BeforeEach
    void setUpFixture() {
        this.owner = this.createUser("Владелец", "owner@example.com");
        this.booker = this.createUser("Арендатор", "booker@example.com");
        this.item = this.createItem("Дрель", "Ударная", true, this.owner);
        this.now = LocalDateTime.now();
    }

    @Test
    void getById_byOwner_fillsLastAndNextBooking() {
        Booking last = this.createBooking(this.item, this.booker,
                this.now.minusDays(2), this.now.minusDays(1), BookingStatus.APPROVED);
        Booking next = this.createBooking(this.item, this.booker,
                this.now.plusDays(1), this.now.plusDays(2), BookingStatus.APPROVED);

        ItemWithBookingsDto found = this.itemService.getById(this.owner.getId(), this.item.getId());

        assertThat(found.getLastBooking()).isNotNull();
        assertThat(found.getLastBooking().getId()).isEqualTo(last.getId());
        assertThat(found.getLastBooking().getBookerId()).isEqualTo(this.booker.getId());
        assertThat(found.getNextBooking()).isNotNull();
        assertThat(found.getNextBooking().getId()).isEqualTo(next.getId());
    }

    @Test
    void getById_byOwner_picksNearestOfSeveralBookings() {
        this.createBooking(this.item, this.booker,
                this.now.minusDays(10), this.now.minusDays(9), BookingStatus.APPROVED);
        Booking nearestPast = this.createBooking(this.item, this.booker,
                this.now.minusDays(2), this.now.minusDays(1), BookingStatus.APPROVED);
        Booking nearestFuture = this.createBooking(this.item, this.booker,
                this.now.plusDays(1), this.now.plusDays(2), BookingStatus.APPROVED);
        this.createBooking(this.item, this.booker,
                this.now.plusDays(10), this.now.plusDays(11), BookingStatus.APPROVED);

        ItemWithBookingsDto found = this.itemService.getById(this.owner.getId(), this.item.getId());

        assertThat(found.getLastBooking().getId()).isEqualTo(nearestPast.getId());
        assertThat(found.getNextBooking().getId()).isEqualTo(nearestFuture.getId());
    }

    @Test
    void getById_byOwner_ignoresNotApprovedBookings() {
        this.createBooking(this.item, this.booker,
                this.now.minusDays(2), this.now.minusDays(1), BookingStatus.REJECTED);
        this.createBooking(this.item, this.booker,
                this.now.plusDays(1), this.now.plusDays(2), BookingStatus.WAITING);

        ItemWithBookingsDto found = this.itemService.getById(this.owner.getId(), this.item.getId());

        assertThat(found.getLastBooking()).isNull();
        assertThat(found.getNextBooking()).isNull();
    }

    @Test
    void getById_byNonOwner_leavesBookingsNull() {
        this.createBooking(this.item, this.booker,
                this.now.minusDays(2), this.now.minusDays(1), BookingStatus.APPROVED);
        this.createBooking(this.item, this.booker,
                this.now.plusDays(1), this.now.plusDays(2), BookingStatus.APPROVED);

        ItemWithBookingsDto found = this.itemService.getById(this.booker.getId(), this.item.getId());

        assertThat(found.getLastBooking()).isNull();
        assertThat(found.getNextBooking()).isNull();
        assertThat(found.getName()).isEqualTo("Дрель");
    }

    @Test
    void getById_withoutBookings_leavesBookingsNullAndCommentsEmpty() {
        ItemWithBookingsDto found = this.itemService.getById(this.owner.getId(), this.item.getId());

        assertThat(found.getLastBooking()).isNull();
        assertThat(found.getNextBooking()).isNull();
        assertThat(found.getComments()).isEmpty();
    }

    @Test
    void getById_whenItemAbsent_throwsNotFound() {
        assertThatThrownBy(() -> this.itemService.getById(this.owner.getId(), 9999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllByOwner_returnsOnlyOwnItemsWithBookings() {
        Item second = this.createItem("Пила", "Циркулярная", true, this.owner);
        User stranger = this.createUser("Посторонний", "stranger@example.com");
        this.createItem("Чужая", "Описание", true, stranger);
        Booking booking = this.createBooking(second, this.booker,
                this.now.minusDays(2), this.now.minusDays(1), BookingStatus.APPROVED);

        var found = this.itemService.getAllByOwner(this.owner.getId());

        assertThat(found).extracting(ItemWithBookingsDto::getName)
                .containsExactly("Дрель", "Пила");
        assertThat(found.get(1).getLastBooking()).isNotNull();
        assertThat(found.get(1).getLastBooking().getId()).isEqualTo(booking.getId());
    }

    @Test
    void getAllByOwner_whenOwnerHasNoItems_returnsEmptyList() {
        User outsider = this.createUser("Без вещей", "outsider@example.com");

        assertThat(this.itemService.getAllByOwner(outsider.getId())).isEmpty();
    }
}
