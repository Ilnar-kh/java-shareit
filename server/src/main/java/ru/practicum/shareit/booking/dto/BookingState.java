package ru.practicum.shareit.booking.dto;

import java.util.Optional;

public enum BookingState {
    ALL,        // Все
    CURRENT,    // Текущие
    PAST,       // Завершённые
    FUTURE,     // Будущие
    WAITING,    // Ожидающие подтверждения
    REJECTED,   // Отклонённые
    CANCELED;   // Отменённые

    public static Optional<BookingState> from(String stringState) {
        for (BookingState state : values()) {
            if (state.name().equalsIgnoreCase(stringState)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}