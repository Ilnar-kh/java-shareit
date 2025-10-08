package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@example.com")
                .build());

        booker = userRepository.save(User.builder()
                .name("Booker")
                .email("booker@example.com")
                .build());

        item = itemRepository.save(Item.builder()
                .name("Drill")
                .description("Cordless drill")
                .available(true)
                .owner(owner)
                .build());
    }

    @Test
    void createApproveAndRetrieveBookingFlow() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);

        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(start)
                .end(end)
                .build();

        BookingResponseDto created = bookingService.create(booker.getId(), dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(created.getItem().getId()).isEqualTo(item.getId());
        assertThat(created.getBooker().getId()).isEqualTo(booker.getId());

        BookingResponseDto approved = bookingService.approve(owner.getId(), created.getId(), true);
        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);

        BookingResponseDto byBooker = bookingService.getById(booker.getId(), created.getId());
        assertThat(byBooker.getStatus()).isEqualTo(BookingStatus.APPROVED);

        List<Long> bookerBookings = bookingService.getForBooker(booker.getId(), BookingState.ALL).stream()
                .map(BookingResponseDto::getId)
                .toList();
        assertThat(bookerBookings).contains(created.getId());

        List<Long> ownerBookings = bookingService.getForOwner(owner.getId(), BookingState.ALL).stream()
                .map(BookingResponseDto::getId)
                .toList();
        assertThat(ownerBookings).contains(created.getId());
    }
}
