package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User requester;
    private User owner;
    private User otherUser;

    @BeforeEach
    void setUp() {
        requester = userRepository.save(User.builder()
                .name("Requester")
                .email("requester@example.com")
                .build());
        owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@example.com")
                .build());
        otherUser = userRepository.save(User.builder()
                .name("Other")
                .email("other@example.com")
                .build());
    }

    @Test
    void createPersistsRequestAndReturnsDto() {
        ItemRequestCreateDto dto = ItemRequestCreateDto.builder()
                .description("Need a cordless drill")
                .build();

        ItemRequestDto result = itemRequestService.create(requester.getId(), dto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Need a cordless drill");
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getItems()).isEmpty();
        assertThat(itemRequestRepository.findById(result.getId())).isPresent();
    }

    @Test
    void getOwnReturnsRequestsWithAssociatedItems() {
        ItemRequest request = itemRequestRepository.save(ItemRequest.builder()
                .description("Need a hammer")
                .requester(requester)
                .created(LocalDateTime.now().minusHours(1))
                .build());

        itemRepository.save(Item.builder()
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .owner(owner)
                .requestId(request.getId())
                .build());

        List<ItemRequestDto> result = itemRequestService.getOwn(requester.getId());

        assertThat(result)
                .hasSize(1)
                .first()
                .satisfies(dto -> {
                    assertThat(dto.getId()).isEqualTo(request.getId());
                    assertThat(dto.getItems())
                            .hasSize(1)
                            .first()
                            .satisfies(item -> {
                                assertThat(item.getId()).isNotNull();
                                assertThat(item.getName()).isEqualTo("Hammer");
                                assertThat(item.getOwnerId()).isEqualTo(owner.getId());
                            });
                });
    }

    @Test
    void getAllReturnsAllRequestsSortedDescending() {
        ItemRequest newer = itemRequestRepository.save(ItemRequest.builder()
                .description("Need a bike")
                .requester(otherUser)
                .created(LocalDateTime.now())
                .build());
        ItemRequest older = itemRequestRepository.save(ItemRequest.builder()
                .description("Need a tent")
                .requester(owner)
                .created(LocalDateTime.now().minusDays(1))
                .build());
        itemRequestRepository.save(ItemRequest.builder()
                .description("Own request")
                .requester(requester)
                .created(LocalDateTime.now().minusHours(2))
                .build());

        List<ItemRequestDto> results = itemRequestService.getAll(requester.getId());

        assertThat(results)
                .extracting(ItemRequestDto::getId)
                .containsExactly(newer.getId(), older.getId());
    }

    @Test
    void getByIdReturnsRequestWithItems() {
        ItemRequest request = itemRequestRepository.save(ItemRequest.builder()
                .description("Need a ladder")
                .requester(otherUser)
                .created(LocalDateTime.now())
                .build());

        itemRepository.save(Item.builder()
                .name("Ladder")
                .description("Aluminium ladder")
                .available(true)
                .owner(owner)
                .requestId(request.getId())
                .build());

        ItemRequestDto dto = itemRequestService.getById(requester.getId(), request.getId());

        assertThat(dto.getId()).isEqualTo(request.getId());
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getName()).isEqualTo("Ladder");
    }
}
