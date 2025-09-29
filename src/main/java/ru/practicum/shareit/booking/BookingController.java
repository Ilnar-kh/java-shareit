package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService service;

    @PostMapping
    public BookingResponseDto create(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                     @RequestBody @Valid BookingDto dto) {
        return service.create(userId, dto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approve(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                      @PathVariable @Positive Long bookingId,
                                      @RequestParam boolean approved) {
        return service.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getById(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                      @PathVariable @Positive Long bookingId) {
        return service.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingResponseDto> getForBooker(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                                 @RequestParam(defaultValue = "ALL") BookingState state) {
        return service.getForBooker(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getForOwner(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                                @RequestParam(defaultValue = "ALL") BookingState state) {
        return service.getForOwner(userId, state);
    }
}
