package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemDto create(Long ownerId, ItemDto dto) {
        if (dto.getAvailable() == null) dto.setAvailable(Boolean.FALSE);

        User owner = userService.getEntityOrThrow(ownerId);

        Item entity = ItemMapper.toEntity(dto);
        entity.setOwner(owner);

        if (dto.getRequestId() != null) {
            itemRequestRepository.findById(dto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос не найден: " + dto.getRequestId()));
            entity.setRequestId(dto.getRequestId());
        }

        Item saved = itemRepository.save(entity);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    @Transactional
    public ItemDto update(Long ownerId, Long itemId, ItemDto patch) {
        Item item = getEntityOrThrow(itemId);
        if (item == null) throw new NotFoundException("Вещь не найдена: " + itemId);
        if (!Objects.equals(item.getOwner().getId(), ownerId))
            throw new ForbiddenException("Редактировать вещь может только её владелец");

        if (patch.getName() != null) item.setName(patch.getName());
        if (patch.getDescription() != null) item.setDescription(patch.getDescription());
        if (patch.getAvailable() != null) item.setAvailable(patch.getAvailable());

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto getById(Long requesterId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + itemId));

        ItemDto dto = ItemMapper.toItemDto(item);

        if (Objects.equals(item.getOwner().getId(), requesterId)) {
            fillBookingDates(dto, item.getId());
        }

        dto.setComments(
                commentRepository.findAllByItemIdOrderByCreatedDesc(itemId).stream()
                        .map(CommentMapper::toDto)
                        .toList()
        );
        return dto;
    }

    @Override
    public List<ItemDto> getByOwner(Long ownerId) {
        return itemRepository.findAllByOwnerIdOrderByIdAsc(ownerId).stream()
                .map(item -> {
                    ItemDto dto = ItemMapper.toItemDto(item);
                    fillBookingDates(dto, item.getId());
                    dto.setComments(
                            commentRepository.findAllByItemIdOrderByCreatedDesc(item.getId()).stream()
                                    .map(CommentMapper::toDto)
                                    .toList()
                    );

                    return dto;
                })
                .toList();
    }


    @Override
    public List<ItemDto> search(String text) {
        if (!StringUtils.hasText(text)) return List.of();
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    public Item getEntityOrThrow(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + id));
    }

    private void fillBookingDates(ItemDto dto, Long itemId) {
        bookingRepository.findLastBooking(itemId).stream().findFirst()
                .ifPresent(booking -> dto.setLastBooking(
                        new BookingShortDto(
                                booking.getId(),
                                booking.getBooker().getId(),
                                booking.getStart(),
                                booking.getEnd()
                        )
                ));

        bookingRepository.findNextBooking(itemId).stream().findFirst()
                .ifPresent(booking -> dto.setNextBooking(
                        new BookingShortDto(
                                booking.getId(),
                                booking.getBooker().getId(),
                                booking.getStart(),
                                booking.getEnd()
                        )
                ));
    }

    @Override
    @Transactional
    public CommentDto addComment(Long authorId, Long itemId, CommentDto dto) {
        var user = userService.getEntityOrThrow(authorId);
        var item = getEntityOrThrow(itemId);

        var now = java.time.LocalDateTime.now();
        boolean canComment = commentRepository.userHasFinishedBooking(authorId, itemId, now);
        if (!canComment) {
            throw new ValidationException("Оставить отзыв может только тот, кто брал вещь и уже вернул её");
        }

        var comment = Comment.builder()
                .text(dto.getText())
                .item(item)
                .author(user)
                .created(now)
                .build();

        var saved = commentRepository.save(comment);
        return CommentMapper.toDto(saved);
    }
}
