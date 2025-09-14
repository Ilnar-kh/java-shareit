package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final Map<Long, User> storage = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    @Override
    public UserDto create(UserDto dto) {
        if (dto.getEmail() == null || dto.getEmail().isBlank())
            throw new ValidationException("Поле email обязательно для заполнения");
        if (storage.values().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(dto.getEmail())))
            throw new ConflictException("Такой email уже используется");

        User entity = UserMapper.toEntity(dto);
        entity.setId(seq.incrementAndGet());
        storage.put(entity.getId(), entity);
        return UserMapper.toUserDto(entity);
    }

    @Override
    public UserDto update(Long id, UserDto patch) {
        User user = storage.get(id);
        if (user == null) throw new NotFoundException("Пользователь не найден: " + id);

        if (patch.getName() != null) user.setName(patch.getName());
        if (patch.getEmail() != null) {
            String newEmail = patch.getEmail();
            if (newEmail.isBlank()) throw new ValidationException("Email не может быть пустым");
            boolean taken = storage.values().stream()
                    .anyMatch(u -> !Objects.equals(u.getId(), id) && u.getEmail().equalsIgnoreCase(newEmail));
            if (taken) throw new ConflictException("Такой email уже используется");
            user.setEmail(newEmail);
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto getById(Long id) {
        User user = storage.get(id);
        if (user == null) throw new NotFoundException("Пользователь не найден: " + id);
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return storage.values().stream().map(UserMapper::toUserDto).toList();
    }

    @Override
    public void delete(Long id) {
        if (storage.remove(id) == null) throw new NotFoundException("Пользователь не найден: " + id);
    }

    public User getEntityOrThrow(Long id) {
        User user = storage.get(id);
        if (user == null) throw new NotFoundException("Пользователь не найден: " + id);
        return user;
    }
}
