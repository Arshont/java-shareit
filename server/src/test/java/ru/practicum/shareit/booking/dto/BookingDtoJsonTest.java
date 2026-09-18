package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void serializes_datesWithoutMillisAndZone() throws Exception {
        UserDto booker = new UserDto();
        booker.setId(2L);
        booker.setName("Арендатор");
        booker.setEmail("booker@example.com");
        BookingDto dto = new BookingDto(
                1L,
                LocalDateTime.of(2030, 1, 1, 10, 0, 0),
                LocalDateTime.of(2030, 1, 2, 11, 30, 45),
                BookingStatus.WAITING,
                booker,
                new ItemDto(1L, "Дрель", "Ударная", true, null));

        String content = this.json.write(dto).getJson();

        assertThat(content).contains("\"start\":\"2030-01-01T10:00:00\"");
        assertThat(content).contains("\"end\":\"2030-01-02T11:30:45\"");
        assertThat(content).contains("\"status\":\"WAITING\"");
        assertThat(content).doesNotContain("Z\"");
        assertThat(content).doesNotContain("+0");
    }

    @Test
    void serializes_nestedBookerAndItemIdentifiers() throws Exception {
        UserDto booker = new UserDto();
        booker.setId(2L);
        booker.setName("Арендатор");
        booker.setEmail("booker@example.com");
        BookingDto dto = new BookingDto(
                1L,
                LocalDateTime.of(2030, 1, 1, 10, 0, 0),
                LocalDateTime.of(2030, 1, 2, 10, 0, 0),
                BookingStatus.APPROVED,
                booker,
                new ItemDto(5L, "Дрель", "Ударная", true, null));

        assertThat(this.json.write(dto)).extractingJsonPathNumberValue("$.booker.id").isEqualTo(2);
        assertThat(this.json.write(dto)).extractingJsonPathNumberValue("$.item.id").isEqualTo(5);
        assertThat(this.json.write(dto)).extractingJsonPathStringValue("$.item.name").isEqualTo("Дрель");
    }
}
