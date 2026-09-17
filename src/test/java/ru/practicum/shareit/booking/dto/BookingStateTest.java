package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingStateTest {

    @Test
    void from_parsesAllKnownValues() {
        assertThat(BookingState.from("ALL")).isEqualTo(BookingState.ALL);
        assertThat(BookingState.from("CURRENT")).isEqualTo(BookingState.CURRENT);
        assertThat(BookingState.from("PAST")).isEqualTo(BookingState.PAST);
        assertThat(BookingState.from("FUTURE")).isEqualTo(BookingState.FUTURE);
        assertThat(BookingState.from("WAITING")).isEqualTo(BookingState.WAITING);
        assertThat(BookingState.from("REJECTED")).isEqualTo(BookingState.REJECTED);
    }

    @Test
    void from_isCaseInsensitive() {
        assertThat(BookingState.from("current")).isEqualTo(BookingState.CURRENT);
    }

    @Test
    void from_withUnknownValue_throwsValidation() {
        assertThatThrownBy(() -> BookingState.from("UNSUPPORTED_STATUS"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("UNSUPPORTED_STATUS");
    }

    @Test
    void from_withNull_throwsValidation() {
        assertThatThrownBy(() -> BookingState.from(null))
                .isInstanceOf(ValidationException.class);
    }
}
