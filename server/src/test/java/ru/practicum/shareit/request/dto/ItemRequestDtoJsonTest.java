package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void serializes_withoutItemsKeyAtAll() throws Exception {
        ItemRequestDto dto = new ItemRequestDto(
                1L, "нужна дрель", LocalDateTime.of(2026, 9, 17, 12, 0, 0));

        String content = this.json.write(dto).getJson();

        assertThat(content).contains("\"id\":1");
        assertThat(content).contains("\"description\":\"нужна дрель\"");
        assertThat(content).contains("\"created\":\"2026-09-17T12:00:00\"");
        assertThat(content).doesNotContain("items");
    }
}
