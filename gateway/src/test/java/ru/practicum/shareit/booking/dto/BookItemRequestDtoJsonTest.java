package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<BookItemRequestDto> json;

    @Test
    void deserializeFromIsoStringsReadsDates() throws Exception {
        String content = "{\"itemId\":5,\"start\":\"2025-01-10T10:00:00\",\"end\":\"2025-01-11T11:00:00\"}";

        BookItemRequestDto dto = json.parseObject(content);

        assertThat(dto.getItemId()).isEqualTo(5L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2025, 1, 10, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2025, 1, 11, 11, 0));
    }

    @Test
    void serializeToIsoStringsPreservesPrecision() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(
                7L,
                LocalDateTime.of(2025, 2, 1, 9, 30, 15),
                LocalDateTime.of(2025, 2, 1, 12, 0, 45)
        );

        JsonContent<BookItemRequestDto> jsonContent = json.write(dto);

        assertThat(jsonContent).extractingJsonPathStringValue("$.start")
                .isEqualTo("2025-02-01T09:30:15");
        assertThat(jsonContent).extractingJsonPathStringValue("$.end")
                .isEqualTo("2025-02-01T12:00:45");
    }
}
