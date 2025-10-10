package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUpdateAndDeleteUserFlow() {
        UserDto created = userService.create(UserDto.builder()
                .name("Alice")
                .email("alice@example.com")
                .build());

        assertThat(created.getId()).isNotNull();
        assertThat(created.getEmail()).isEqualTo("alice@example.com");

        UserDto updated = userService.update(created.getId(), UserDto.builder()
                .name("Alice Updated")
                .build());

        assertThat(updated.getName()).isEqualTo("Alice Updated");
        assertThat(updated.getEmail()).isEqualTo("alice@example.com");

        UserDto fetched = userService.getById(created.getId());
        assertThat(fetched.getName()).isEqualTo("Alice Updated");

        List<UserDto> all = userService.getAll();
        assertThat(all).extracting(UserDto::getId).contains(created.getId());

        userService.delete(created.getId());
        assertThat(userRepository.existsById(created.getId())).isFalse();
    }
}
