package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User requester;
    private User booker;
    private ItemRequest request;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();

        owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@example.com")
                .build());

        requester = userRepository.save(User.builder()
                .name("Requester")
                .email("requester@example.com")
                .build());

        booker = userRepository.save(User.builder()
                .name("Booker")
                .email("booker@example.com")
                .build());

        request = itemRequestRepository.save(ItemRequest.builder()
                .description("Need a drill")
                .requester(requester)
                .created(LocalDateTime.now().minusDays(1))
                .build());
    }

    @Test
    void createItemWithRequestAndAddCommentFlow() {
        ItemDto dto = ItemDto.builder()
                .name("Drill")
                .description("Cordless drill")
                .available(true)
                .requestId(request.getId())
                .build();

        ItemDto created = itemService.create(owner.getId(), dto);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getRequestId()).isEqualTo(request.getId());

        Item persistedItem = itemRepository.findById(created.getId()).orElseThrow();

        LocalDateTime now = LocalDateTime.now();
        bookingRepository.save(Booking.builder()
                .item(persistedItem)
                .booker(booker)
                .start(now.minusDays(3))
                .end(now.minusDays(1))
                .status(BookingStatus.APPROVED)
                .build());

        CommentDto comment = CommentDto.builder()
                .text("Отличная вещь")
                .build();

        CommentDto savedComment = itemService.addComment(booker.getId(), persistedItem.getId(), comment);
        assertThat(savedComment.getId()).isNotNull();
        assertThat(savedComment.getAuthorId()).isEqualTo(booker.getId());
        assertThat(savedComment.getAuthorName()).isEqualTo(booker.getName());

        ItemDto detailed = itemService.getById(owner.getId(), persistedItem.getId());
        assertThat(detailed.getComments()).extracting(CommentDto::getText).contains("Отличная вещь");
        assertThat(detailed.getLastBooking()).isNotNull();
    }
}
