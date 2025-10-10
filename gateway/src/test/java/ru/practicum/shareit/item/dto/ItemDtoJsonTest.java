package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    void deserializeWithoutRequestIdSetsNull() throws Exception {
        String content = "{\"name\":\"Drill\",\"description\":\"Cordless\",\"available\":true}";

        ItemDto dto = json.parseObject(content);

        assertThat(dto.getName()).isEqualTo("Drill");
        assertThat(dto.getRequestId()).isNull();
    }

    @Test
    void serializeWithRequestIdContainsField() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(7L)
                .name("Drill")
                .description("Cordless")
                .available(true)
                .requestId(42L)
                .build();

        JsonContent<ItemDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(7);
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(42);
    }
}
