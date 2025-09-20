package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(UserDto dto) {
        if (dto.getEmail() == null || dto.getEmail().isBlank())
            throw new ValidationException("Поле email обязательно для заполнения");

        if (userRepository.existsByEmailIgnoreCase(dto.getEmail()))
            throw new ConflictException("Такой email уже используется");

        User entity = UserMapper.toEntity(dto);
        User saved = userRepository.save(entity);
        return UserMapper.toUserDto(saved);
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserDto patch) {
        User user = getEntityOrThrow(id);

        if (patch.getName() != null) {
            user.setName(patch.getName());
        }
        if (patch.getEmail() != null) {
            String newEmail = patch.getEmail();
            if (newEmail.isBlank()) {
                throw new ValidationException("Email не может быть пустым");
            }
            if (userRepository.existsByEmailIgnoreCase(newEmail)
                    && !newEmail.equalsIgnoreCase(user.getEmail())) {
                throw new ConflictException("Такой email уже используется");
            }
            user.setEmail(newEmail);
        }

        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto getById(Long id) {
        return UserMapper.toUserDto(getEntityOrThrow(id));
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id))
            throw new NotFoundException("Пользователь не найден: " + id);
        userRepository.deleteById(id);
    }

    public User getEntityOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
    }
}
