package ru.practicum.shareit.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException user(Long userId) {
        return new NotFoundException("Пользователь с id=%d не найден".formatted(userId));
    }

    public static NotFoundException item(Long itemId) {
        return new NotFoundException("Вещь с id=%d не найдена".formatted(itemId));
    }
}
