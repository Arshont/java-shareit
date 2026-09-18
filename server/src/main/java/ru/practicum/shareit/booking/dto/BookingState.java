package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.exception.ValidationException;

import java.util.Arrays;

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static BookingState from(String value) {
        return Arrays.stream(values())
                .filter(state -> state.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Unknown state: " + value));
    }
}
