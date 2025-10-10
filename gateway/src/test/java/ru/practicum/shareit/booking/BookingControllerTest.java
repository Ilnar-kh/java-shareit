package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.exceptions.ErrorHandler;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@Import(ErrorHandler.class)
class BookingControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void createBookingWithValidDatesForwardsToClient() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(4L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));

        when(bookingClient.bookItem(eq(1L), any(BookItemRequestDto.class))).thenReturn(ResponseEntity.ok(Map.of("id", 10)));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(bookingClient).bookItem(eq(1L), any(BookItemRequestDto.class));
    }

    @Test
    void createBookingWithInvalidDatesReturnsBadRequest() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(4L, LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(1));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).bookItem(anyLong(), any(BookItemRequestDto.class));
    }

    @Test
    void getBookingsWithUnknownStateReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, "1")
                        .param("state", "mystery"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unknown state: mystery"));

        verify(bookingClient, never()).getBookings(anyLong(), any(), any(), any());
    }
}
