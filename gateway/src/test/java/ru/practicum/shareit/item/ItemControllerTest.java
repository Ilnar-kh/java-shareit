package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.ErrorHandler;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@Import(ErrorHandler.class)
class ItemControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    void createItemWithValidPayloadCallsClient() throws Exception {
        ItemDto dto = ItemDto.builder()
                .name("Drill")
                .description("Cordless drill")
                .available(true)
                .requestId(5L)
                .build();

        when(itemClient.create(eq(1L), any(ItemDto.class))).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        ArgumentCaptor<ItemDto> captor = ArgumentCaptor.forClass(ItemDto.class);
        verify(itemClient).create(eq(1L), captor.capture());
        assertThat(captor.getValue().getRequestId()).isEqualTo(5L);
    }

    @Test
    void createItemWithInvalidBodyReturnsBadRequest() throws Exception {
        ItemDto dto = ItemDto.builder()
                .name("")
                .description("")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).create(anyLong(), any(ItemDto.class));
    }

    @Test
    void addCommentWithValidBodyCallsClient() throws Exception {
        CommentDto dto = CommentDto.builder()
                .text("Great item")
                .build();

        when(itemClient.addComment(eq(2L), eq(3L), any(CommentDto.class))).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items/3/comment")
                        .header(USER_HEADER, "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(itemClient).addComment(eq(2L), eq(3L), any(CommentDto.class));
    }
}