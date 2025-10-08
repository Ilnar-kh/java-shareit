package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserService userService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .name("owner")
                .email("owner@example.com")
                .build();
    }

    @Test
    void createWithRequestIdLinksRequestToItem() {
        ItemDto dto = ItemDto.builder()
                .name("Drill")
                .description("Simple drill")
                .available(true)
                .requestId(42L)
                .build();

        when(userService.getEntityOrThrow(1L)).thenReturn(owner);
        when(itemRequestRepository.findById(42L))
                .thenReturn(Optional.of(ItemRequest.builder().id(42L).build()));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0, Item.class);
            item.setId(10L);
            return item;
        });

        ItemDto result = itemService.create(1L, dto);

        assertThat(result.getRequestId()).isEqualTo(42L);

        verify(itemRequestRepository).findById(42L);

        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(itemCaptor.capture());
        Item savedEntity = itemCaptor.getValue();
        assertThat(savedEntity.getRequestId()).isEqualTo(42L);
        assertThat(savedEntity.getOwner()).isEqualTo(owner);
    }

    @Test
    void createWithoutRequestIdSucceedsWithoutLookup() {
        ItemDto dto = ItemDto.builder()
                .name("Hammer")
                .description("Just a hammer")
                .available(true)
                .build();

        when(userService.getEntityOrThrow(1L)).thenReturn(owner);
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0, Item.class);
            item.setId(11L);
            return item;
        });

        ItemDto result = itemService.create(1L, dto);

        assertThat(result.getRequestId()).isNull();
        verify(itemRequestRepository, never()).findById(anyLong());
    }

    @Test
    void create_withUnknownRequestThrowsNotFound() {
        ItemDto dto = ItemDto.builder()
                .name("Saw")
                .description("Sharp saw")
                .available(true)
                .requestId(77L)
                .build();

        when(userService.getEntityOrThrow(1L)).thenReturn(owner);
        when(itemRequestRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.create(1L, dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("77");

        verify(itemRepository, never()).save(any());
    }
}
