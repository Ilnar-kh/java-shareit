package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.ErrorHandler;
import ru.practicum.shareit.user.dto.UserDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(ErrorHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @Test
    void createUserWithValidPayloadReturnsOk() throws Exception {
        UserDto dto = UserDto.builder()
                .name("Alice")
                .email("alice@example.com")
                .build();

        when(userClient.create(any(UserDto.class))).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(userClient).create(any(UserDto.class));
    }

    @Test
    void createUserWithInvalidEmailReturnsBadRequest() throws Exception {
        UserDto dto = UserDto.builder()
                .name("Alice")
                .email("not-an-email")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).create(any(UserDto.class));
    }

    @Test
    void deleteUserWithInvalidIdReturnsBadRequest() throws Exception {
        mockMvc.perform(delete("/users/0"))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).delete(anyLong());
    }

    @Test
    void updateUserWithPositiveIdCallsClient() throws Exception {
        UserDto patchDto = UserDto.builder()
                .name("Bob")
                .build();

        when(userClient.update(eq(3L), any(UserDto.class))).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/users/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk());

        verify(userClient).update(eq(3L), any(UserDto.class));
    }
}
