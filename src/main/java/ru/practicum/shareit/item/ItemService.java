package ru.practicum.shareit.item;

import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDto create(Long ownerId, ItemDto dto);

    ItemDto update(Long ownerId, Long itemId, ItemDto patch);

    ItemDto getById(Long requesterId, Long itemId);

    List<ItemDto> getByOwner(Long ownerId);

    List<ItemDto> search(String text);

    Item getEntityOrThrow(Long id);

    CommentDto addComment(Long authorId, Long itemId, CommentDto dto);
}
