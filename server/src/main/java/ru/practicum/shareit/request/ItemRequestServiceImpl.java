package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.user.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto create(Long requesterId, ItemRequestCreateDto dto) {
        var requester = userService.getEntityOrThrow(requesterId);
        var entity = ItemRequestMapper.toEntity(dto, requester);
        var saved = itemRequestRepository.save(entity);
        return ItemRequestMapper.toDto(saved, Collections.emptyList());
    }

    @Override
    public List<ItemRequestDto> getOwn(Long requesterId) {
        userService.getEntityOrThrow(requesterId);
        var requests = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(requesterId);
        return mapWithItems(requests);
    }

    @Override
    public List<ItemRequestDto> getAll(Long requesterId) {
        userService.getEntityOrThrow(requesterId);
        var requests = itemRequestRepository.findAllByRequesterIdNotOrderByCreatedDesc(requesterId);
        return mapWithItems(requests);
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        userService.getEntityOrThrow(userId);
        var request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден: " + requestId));
        var items = itemRepository.findAllByRequestIdIn(List.of(request.getId()));
        return ItemRequestMapper.toDto(request, items);
    }

    private List<ItemRequestDto> mapWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }

        var requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        Map<Long, List<ru.practicum.shareit.item.model.Item>> itemsByRequest = itemRepository.findAllByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(ru.practicum.shareit.item.model.Item::getRequestId));

        return requests.stream()
                .map(request -> ItemRequestMapper.toDto(request, itemsByRequest.getOrDefault(request.getId(), List.of())))
                .toList();
    }
}