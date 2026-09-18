package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemWithBookingsDtoJsonTest {

    @Autowired
    private JacksonTester<ItemWithBookingsDto> json;

    @Test
    void serializes_bookingKeysPresentEvenWhenNull() throws Exception {
        ItemWithBookingsDto dto = new ItemWithBookingsDto(
                1L, "Дрель", "Ударная", true, null, null, null, List.of());

        String content = this.json.write(dto).getJson();

        assertThat(content).contains("\"lastBooking\":null");
        assertThat(content).contains("\"nextBooking\":null");
        assertThat(content).contains("\"comments\":[]");
    }

    @Test
    void serializes_requestIdKeyPresentEvenWhenNull() throws Exception {
        ItemWithBookingsDto dto = new ItemWithBookingsDto(
                1L, "Дрель", "Ударная", true, null, null, null, List.of());

        assertThat(this.json.write(dto).getJson()).contains("\"requestId\":null");
    }
}
