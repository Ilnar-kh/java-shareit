package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.util.List;

public interface BookingService {
    BookingResponseDto create(Long bookerId, BookingDto dto);

    BookingResponseDto approve(Long ownerId, Long bookingId, boolean approved);

    BookingResponseDto getById(Long requesterId, Long bookingId);

    List<BookingResponseDto> getForBooker(Long bookerId, BookingState state);

    List<BookingResponseDto> getForOwner(Long ownerId, BookingState state);
}
