package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.AbstractIntegrationTest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookingRepositoryTest extends AbstractIntegrationTest {

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
    void findAllByBookerId_returnsBookingsNewestFirst() {
        this.createBooking(this.item, this.booker,
                this.now.plusDays(1), this.now.plusDays(2), BookingStatus.WAITING);
        this.createBooking(this.item, this.booker,
                this.now.plusDays(5), this.now.plusDays(6), BookingStatus.WAITING);

        List<Booking> found = this.bookingRepository.findAllByBookerIdOrderByStartDesc(this.booker.getId());

        assertThat(found).hasSize(2);
        assertThat(found.get(0).getStart()).isAfter(found.get(1).getStart());
    }

    @Test
    void findAllByBookerIdAndEndBefore_returnsOnlyFinished() {
        Booking past = this.createBooking(this.item, this.booker,
                this.now.minusDays(5), this.now.minusDays(4), BookingStatus.APPROVED);
        this.createBooking(this.item, this.booker,
                this.now.plusDays(1), this.now.plusDays(2), BookingStatus.APPROVED);

        List<Booking> found = this.bookingRepository
                .findAllByBookerIdAndEndBeforeOrderByStartDesc(this.booker.getId(), this.now);

        assertThat(found).extracting(Booking::getId).containsExactly(past.getId());
    }

    @Test
    void findAllByBookerIdAndStartAfter_returnsOnlyFuture() {
        this.createBooking(this.item, this.booker,
                this.now.minusDays(5), this.now.minusDays(4), BookingStatus.APPROVED);
        Booking future = this.createBooking(this.item, this.booker,
                this.now.plusDays(1), this.now.plusDays(2), BookingStatus.APPROVED);

        List<Booking> found = this.bookingRepository
                .findAllByBookerIdAndStartAfterOrderByStartDesc(this.booker.getId(), this.now);

        assertThat(found).extracting(Booking::getId).containsExactly(future.getId());
    }

    @Test
    void findAllByBookerIdAndStartBeforeAndEndAfter_returnsOnlyCurrent() {
        Booking current = this.createBooking(this.item, this.booker,
                this.now.minusDays(1), this.now.plusDays(1), BookingStatus.APPROVED);
        this.createBooking(this.item, this.booker,
                this.now.plusDays(5), this.now.plusDays(6), BookingStatus.APPROVED);

        List<Booking> found = this.bookingRepository
                .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        this.booker.getId(), this.now, this.now);

        assertThat(found).extracting(Booking::getId).containsExactly(current.getId());
    }

    @Test
    void findAllByBookerIdAndStatus_filtersByStatus() {
        Booking rejected = this.createBooking(this.item, this.booker,
                this.now.plusDays(1), this.now.plusDays(2), BookingStatus.REJECTED);
        this.createBooking(this.item, this.booker,
                this.now.plusDays(3), this.now.plusDays(4), BookingStatus.WAITING);

        List<Booking> found = this.bookingRepository
                .findAllByBookerIdAndStatusOrderByStartDesc(this.booker.getId(), BookingStatus.REJECTED);

        assertThat(found).extracting(Booking::getId).containsExactly(rejected.getId());
    }

    @Test
    void findAllByItemOwnerId_walksNestedPath() {
        Booking booking = this.createBooking(this.item, this.booker,
                this.now.plusDays(1), this.now.plusDays(2), BookingStatus.WAITING);

        List<Booking> found = this.bookingRepository.findAllByItemOwnerIdOrderByStartDesc(this.owner.getId());

        assertThat(found).extracting(Booking::getId).containsExactly(booking.getId());
    }

    @Test
    void findAllByItemIdAndStatus_returnsBookingsOfItem() {
        Booking approved = this.createBooking(this.item, this.booker,
                this.now.plusDays(1), this.now.plusDays(2), BookingStatus.APPROVED);
        this.createBooking(this.item, this.booker,
                this.now.plusDays(3), this.now.plusDays(4), BookingStatus.WAITING);

        List<Booking> found = this.bookingRepository
                .findAllByItemIdAndStatusOrderByStart(this.item.getId(), BookingStatus.APPROVED);

        assertThat(found).extracting(Booking::getId).containsExactly(approved.getId());
    }

    @Test
    void findAllByItemIdInAndStatus_returnsBookingsOfSeveralItems() {
        Item second = this.createItem("Пила", "Циркулярная", true, this.owner);
        Booking first = this.createBooking(this.item, this.booker,
                this.now.plusDays(1), this.now.plusDays(2), BookingStatus.APPROVED);
        Booking secondBooking = this.createBooking(second, this.booker,
                this.now.plusDays(3), this.now.plusDays(4), BookingStatus.APPROVED);

        List<Booking> found = this.bookingRepository.findAllByItemIdInAndStatusOrderByStart(
                List.of(this.item.getId(), second.getId()), BookingStatus.APPROVED);

        assertThat(found).extracting(Booking::getId)
                .containsExactlyInAnyOrder(first.getId(), secondBooking.getId());
    }

    @Test
    void existsByBookerIdAndItemIdAndStatusAndEndBefore_trueForFinishedApproved() {
        this.createBooking(this.item, this.booker,
                this.now.minusDays(5), this.now.minusDays(4), BookingStatus.APPROVED);

        boolean exists = this.bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                this.booker.getId(), this.item.getId(), BookingStatus.APPROVED, this.now);

        assertThat(exists).isTrue();
    }

    @Test
    void existsByBookerIdAndItemIdAndStatusAndEndBefore_falseForUnfinished() {
        this.createBooking(this.item, this.booker,
                this.now.plusDays(1), this.now.plusDays(2), BookingStatus.APPROVED);

        boolean exists = this.bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                this.booker.getId(), this.item.getId(), BookingStatus.APPROVED, this.now);

        assertThat(exists).isFalse();
    }
}
