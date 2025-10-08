package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
@Import(ErrorHandler.class)
class ItemRequestControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Test
    void createRequestWithValidPayloadCallsClient() throws Exception {
        ItemRequestCreateDto dto = ItemRequestCreateDto.builder()
                .description("Need a drill")
                .build();

        when(itemRequestClient.create(eq(1L), any(ItemRequestCreateDto.class))).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(itemRequestClient).create(eq(1L), any(ItemRequestCreateDto.class));
    }

    @Test
    void createRequestWithBlankDescriptionReturnsBadRequest() throws Exception {
        ItemRequestCreateDto dto = ItemRequestCreateDto.builder()
                .description("")
                .build();

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).create(anyLong(), any());
    }

    @Test
    void getOwnRequestsCallsClient() throws Exception {
        when(itemRequestClient.getOwn(3L)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests")
                        .header(USER_HEADER, "3"))
                .andExpect(status().isOk());

        verify(itemRequestClient).getOwn(3L);
    }

    @Test
    void getAllRequestsCallsClient() throws Exception {
        when(itemRequestClient.getAll(5L)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/all")
                        .header(USER_HEADER, "5"))
                .andExpect(status().isOk());

        verify(itemRequestClient).getAll(5L);
    }


    @Test
    void getRequestByIdCallsClient() throws Exception {
        when(itemRequestClient.getById(7L, 9L)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/9")
                        .header(USER_HEADER, "7"))
                .andExpect(status().isOk());

        verify(itemRequestClient).getById(7L, 9L);
    }
}
