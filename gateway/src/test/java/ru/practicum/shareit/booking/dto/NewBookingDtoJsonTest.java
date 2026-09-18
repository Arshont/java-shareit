package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class NewBookingDtoJsonTest {

    @Autowired
    private JacksonTester<NewBookingDto> json;

    @Test
    void deserializes_dateWithoutZoneAndMillis() throws Exception {
        String content = "{\"itemId\":1,\"start\":\"2030-01-01T10:00:00\","
                + "\"end\":\"2030-01-02T11:30:45\"}";

        NewBookingDto dto = this.json.parseObject(content);

        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2030, 1, 1, 10, 0, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2030, 1, 2, 11, 30, 45));
    }

    @Test
    void isStartBeforeEnd_isTrueWhenEitherDateIsNull() throws Exception {
        NewBookingDto dto = this.json.parseObject("{\"itemId\":1}");

        assertThat(dto.isStartBeforeEnd()).isTrue();
    }

    @Test
    void isStartBeforeEnd_isFalseWhenStartEqualsEnd() throws Exception {
        NewBookingDto dto = this.json.parseObject(
                "{\"itemId\":1,\"start\":\"2030-01-01T10:00:00\",\"end\":\"2030-01-01T10:00:00\"}");

        assertThat(dto.isStartBeforeEnd()).isFalse();
    }

    @Test
    void serializes_withoutSyntheticValidationProperty() throws Exception {
        NewBookingDto dto = new NewBookingDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.of(2030, 1, 1, 10, 0, 0));
        dto.setEnd(LocalDateTime.of(2030, 1, 2, 10, 0, 0));

        String content = this.json.write(dto).getJson();

        assertThat(content).contains("\"itemId\":1");
        assertThat(content).doesNotContain("startBeforeEnd");
    }
}
