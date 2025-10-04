package ru.practicum.shareit.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestItemDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemRequestMapper {

    public static ItemRequest toEntity(ItemRequestCreateDto dto, User requester) {
        if (dto == null) return null;
        return ItemRequest.builder()
                .description(dto.getDescription())
                .requester(requester)
                .created(LocalDateTime.now())
                .build();
    }

    public static ItemRequestDto toDto(ItemRequest request, List<Item> items) {
        if (request == null) return null;
        List<ItemRequestItemDto> responses = items == null ? Collections.emptyList() :
                items.stream()
                        .sorted(Comparator.comparing(Item::getId))
                        .map(ItemRequestMapper::toItemDto)
                        .toList();

        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(responses)
                .build();
    }

    private static ItemRequestItemDto toItemDto(Item item) {
        if (item == null) return null;
        return ItemRequestItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .ownerId(item.getOwner().getId())
                .build();
    }
}
