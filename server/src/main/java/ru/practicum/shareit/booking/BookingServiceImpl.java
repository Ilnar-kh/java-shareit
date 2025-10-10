package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    private static final Sort SORT_DESC = Sort.by(Sort.Direction.DESC, "start");

    @Override
    @Transactional
    public BookingResponseDto create(Long bookerId, BookingDto dto) {
        if (dto.getStart() == null || dto.getEnd() == null || !dto.getEnd().isAfter(dto.getStart())) {
            throw new ValidationException("Некорректный интервал бронирования");
        }

        User booker = userService.getEntityOrThrow(bookerId);
        Item item = itemService.getEntityOrThrow(dto.getItemId());

        if (item.getOwner().getId().equals(bookerId)) {
            throw new ForbiddenException("Нельзя бронировать свою вещь");
        }
        if (Boolean.FALSE.equals(item.getAvailable())) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        Booking entity = BookingMapper.toEntity(dto, booker, item);
        entity.setStatus(BookingStatus.WAITING);

        return BookingMapper.toResponseDto(bookingRepository.save(entity));
    }

    @Override
    @Transactional
    public BookingResponseDto approve(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено: " + bookingId));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Подтверждать может только владелец вещи");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ConflictException("Статус уже установлен");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto getById(Long requesterId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено: " + bookingId));

        Long ownerId = booking.getItem().getOwner().getId();
        Long bookerId = booking.getBooker().getId();
        if (!ownerId.equals(requesterId) && !bookerId.equals(requesterId)) {
            throw new ForbiddenException("Доступ запрещён");
        }

        return BookingMapper.toResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getForBooker(Long bookerId, BookingState state) {
        userService.getEntityOrThrow(bookerId);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findAllByBookerId(bookerId, SORT_DESC);
            case CURRENT -> bookingRepository.findCurrentForBooker(bookerId, now, SORT_DESC);
            case PAST -> bookingRepository.findByBookerIdAndEndIsBefore(bookerId, now, SORT_DESC);
            case FUTURE -> bookingRepository.findByBookerIdAndStartIsAfter(bookerId, now, SORT_DESC);
            case WAITING -> bookingRepository.findByBookerIdAndStatus(bookerId, BookingStatus.WAITING, SORT_DESC);
            case REJECTED -> bookingRepository.findByBookerIdAndStatus(bookerId, BookingStatus.REJECTED, SORT_DESC);
            case CANCELED -> bookingRepository.findByBookerIdAndStatus(bookerId, BookingStatus.CANCELED, SORT_DESC);
        };
        return bookings.stream().map(BookingMapper::toResponseDto).toList();
    }

    @Override
    public List<BookingResponseDto> getForOwner(Long ownerId, BookingState state) {
        userService.getEntityOrThrow(ownerId);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findAllForOwner(ownerId, SORT_DESC);
            case CURRENT -> bookingRepository.findCurrentForOwner(ownerId, now, SORT_DESC);
            case PAST -> bookingRepository.findPastForOwner(ownerId, now, SORT_DESC);
            case FUTURE -> bookingRepository.findFutureForOwner(ownerId, now, SORT_DESC);
            case WAITING -> bookingRepository.findByOwnerAndStatus(ownerId, BookingStatus.WAITING, SORT_DESC);
            case REJECTED -> bookingRepository.findByOwnerAndStatus(ownerId, BookingStatus.REJECTED, SORT_DESC);
            case CANCELED -> bookingRepository.findByOwnerAndStatus(ownerId, BookingStatus.CANCELED, SORT_DESC);
        };
        return bookings.stream().map(BookingMapper::toResponseDto).toList();
    }
}
