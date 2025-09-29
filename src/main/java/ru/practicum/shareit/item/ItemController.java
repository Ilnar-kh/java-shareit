package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@Validated
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService service;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto create(@RequestHeader(USER_HEADER) @Positive Long userId,
                          @Valid @RequestBody ItemDto dto) {
        return service.create(userId, dto);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                 @PathVariable @Positive Long itemId,
                                 @RequestBody @Valid CommentDto dto) {
        return service.addComment(userId, itemId, dto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_HEADER) @Positive Long userId,
                          @PathVariable @Positive Long itemId,
                          @RequestBody ItemDto patch) {
        return service.update(userId, itemId, patch);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@RequestHeader(USER_HEADER) @Positive Long userId,
                           @PathVariable @Positive Long itemId) {
        return service.getById(userId, itemId);
    }

    @GetMapping
    public List<ItemDto> getOwnerItems(@RequestHeader(USER_HEADER) @Positive Long userId) {
        return service.getByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return service.search(text);
    }
}
