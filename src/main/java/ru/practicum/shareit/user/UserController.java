package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService service;

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto dto) {
        return service.create(dto);
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable @Positive Long id, @RequestBody UserDto patch) {
        return service.update(id, patch);
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable @Positive Long id) {
        return service.getById(id);
    }

    @GetMapping
    public List<UserDto> getAll() {
        return service.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable @Positive Long id) {
        service.delete(id);
    }
}