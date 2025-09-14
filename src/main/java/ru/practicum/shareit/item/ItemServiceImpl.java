package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final Map<Long, Item> storage = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    private final UserService userService;

    @Override
    public ItemDto create(Long ownerId, ItemDto dto) {
        if (dto.getAvailable() == null) dto.setAvailable(Boolean.FALSE);

        User owner = userService.getEntityOrThrow(ownerId);

        Item entity = ItemMapper.toEntity(dto);
        entity.setId(seq.incrementAndGet());
        entity.setOwner(owner);
        storage.put(entity.getId(), entity);
        return ItemMapper.toItemDto(entity);
    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto patch) {
        Item item = storage.get(itemId);
        if (item == null) throw new NotFoundException("Вещь не найдена: " + itemId);
        if (!Objects.equals(item.getOwner().getId(), ownerId))
            throw new ForbiddenException("Редактировать вещь может только её владелец");

        if (patch.getName() != null) item.setName(patch.getName());
        if (patch.getDescription() != null) item.setDescription(patch.getDescription());
        if (patch.getAvailable() != null) item.setAvailable(patch.getAvailable());

        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getById(Long requesterId, Long itemId) {
        Item item = storage.get(itemId);
        if (item == null) throw new NotFoundException("Вещь не найдена: " + itemId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getByOwner(Long ownerId) {
        return storage.values().stream()
                .filter(i -> i.getOwner() != null && Objects.equals(i.getOwner().getId(), ownerId))
                .sorted(Comparator.comparing(Item::getId))
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (!StringUtils.hasText(text)) return List.of();
        String q = text.toLowerCase();
        return storage.values().stream()
                .filter(Item::getAvailable)
                .filter(i ->
                        (i.getName() != null && i.getName().toLowerCase().contains(q)) ||
                                (i.getDescription() != null && i.getDescription().toLowerCase().contains(q))
                )
                .sorted(Comparator.comparing(Item::getId))
                .map(ItemMapper::toItemDto)
                .toList();
    }

    public Item getEntityOrThrow(Long id) {
        Item item = storage.get(id);
        if (item == null) throw new NotFoundException("Вещь не найдена: " + id);
        return item;
    }
}
