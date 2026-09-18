package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.ItemAnswerDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestWithItemsDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestWithItemsDto> json;

    @Test
    void serializes_createdWithoutZoneAndAnswersWithOwnerId() throws Exception {
        ItemRequestWithItemsDto dto = new ItemRequestWithItemsDto(
                7L, "нужна дрель", LocalDateTime.of(2026, 9, 17, 12, 0, 0),
                List.of(new ItemAnswerDto(3L, "Дрель Bosch", 2L)));

        String content = this.json.write(dto).getJson();

        assertThat(content).contains("\"created\":\"2026-09-17T12:00:00\"");
        assertThat(this.json.write(dto)).extractingJsonPathStringValue("$.items[0].name")
                .isEqualTo("Дрель Bosch");
        assertThat(this.json.write(dto)).extractingJsonPathNumberValue("$.items[0].ownerId")
                .isEqualTo(2);
    }

    @Test
    void serializes_emptyAnswersAsEmptyArrayNotNull() throws Exception {
        ItemRequestWithItemsDto dto = new ItemRequestWithItemsDto(
                7L, "нужна дрель", LocalDateTime.of(2026, 9, 17, 12, 0, 0), List.of());

        assertThat(this.json.write(dto).getJson()).contains("\"items\":[]");
    }
}
