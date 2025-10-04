package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto create(Long requesterId, ItemRequestCreateDto dto);

    List<ItemRequestDto> getOwn(Long requesterId);

    List<ItemRequestDto> getAll(Long requesterId);

    ItemRequestDto getById(Long userId, Long requestId);
}