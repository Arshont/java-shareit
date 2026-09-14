package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingMapperTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 10, 1, 12, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 10, 2, 12, 0, 0);

    @Test
    void toBookingDto_copiesFieldsAndNestsBookerAndItem() {
        Booking booking = this.booking();

        BookingDto dto = BookingMapper.toBookingDto(booking);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getStart()).isEqualTo(START);
        assertThat(dto.getEnd()).isEqualTo(END);
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(dto.getBooker()).isNotNull();
        assertThat(dto.getBooker().getId()).isEqualTo(2L);
        assertThat(dto.getItem()).isNotNull();
        assertThat(dto.getItem().getId()).isEqualTo(3L);
        assertThat(dto.getItem().getName()).isEqualTo("Дрель");
    }

    @Test
    void toBookingShortDto_keepsOnlyIdAndBookerId() {
        Booking booking = this.booking();

        BookingShortDto dto = BookingMapper.toBookingShortDto(booking);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getBookerId()).isEqualTo(2L);
    }

    private Booking booking() {
        User owner = User.builder().id(1L).name("Владелец").email("owner@example.com").build();
        User booker = User.builder().id(2L).name("Арендатор").email("booker@example.com").build();
        Item item = Item.builder()
                .id(3L)
                .name("Дрель")
                .description("Ударная")
                .available(true)
                .owner(owner)
                .build();
        return Booking.builder()
                .id(10L)
                .start(START)
                .end(END)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
    }
}
