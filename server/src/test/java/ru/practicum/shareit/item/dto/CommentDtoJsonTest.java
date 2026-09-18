package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    void serializes_authorNameAsFlatStringAndCreatedWithoutZone() throws Exception {
        CommentDto dto = new CommentDto(1L, "Отличная дрель", "Иван",
                LocalDateTime.of(2026, 9, 17, 12, 0, 0));

        String content = this.json.write(dto).getJson();

        assertThat(content).contains("\"authorName\":\"Иван\"");
        assertThat(content).contains("\"created\":\"2026-09-17T12:00:00\"");
        assertThat(content).doesNotContain("\"author\":");
    }
}
