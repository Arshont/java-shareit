package ru.practicum.shareit.request.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemAnswerDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRequestMapperTest {

    private static final LocalDateTime CREATED = LocalDateTime.of(2026, 9, 17, 12, 0, 0);

    @Test
    void toItemRequest_copiesDescriptionAndSetsRequestorAndCreated() {
        User anna = User.builder().id(1L).name("Анна").email("anna@example.com").build();
        NewItemRequestDto request = new NewItemRequestDto();
        request.setDescription("нужна дрель");

        ItemRequest entity = ItemRequestMapper.toItemRequest(request, anna, CREATED);

        assertThat(entity.getDescription()).isEqualTo("нужна дрель");
        assertThat(entity.getRequestor()).isEqualTo(anna);
        assertThat(entity.getCreated()).isEqualTo(CREATED);
        assertThat(entity.getId()).isNull();
    }

    @Test
    void toItemRequestDto_copiesThreeFields() {
        User anna = User.builder().id(1L).name("Анна").email("anna@example.com").build();
        ItemRequest entity = ItemRequest.builder()
                .id(7L).description("нужна дрель").requestor(anna).created(CREATED).build();

        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(entity);

        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getDescription()).isEqualTo("нужна дрель");
        assertThat(dto.getCreated()).isEqualTo(CREATED);
    }

    @Test
    void toItemRequestWithItemsDto_attachesAnswers() {
        User anna = User.builder().id(1L).name("Анна").email("anna@example.com").build();
        ItemRequest entity = ItemRequest.builder()
                .id(7L).description("нужна дрель").requestor(anna).created(CREATED).build();
        List<ItemAnswerDto> answers = List.of(new ItemAnswerDto(3L, "Дрель Bosch", 2L));

        ItemRequestWithItemsDto dto = ItemRequestMapper.toItemRequestWithItemsDto(entity, answers);

        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getCreated()).isEqualTo(CREATED);
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getName()).isEqualTo("Дрель Bosch");
        assertThat(dto.getItems().get(0).getOwnerId()).isEqualTo(2L);
    }

    @Test
    void toItemRequestWithItemsDto_withNoAnswersGivesEmptyList() {
        User anna = User.builder().id(1L).name("Анна").email("anna@example.com").build();
        ItemRequest entity = ItemRequest.builder()
                .id(7L).description("нужна дрель").requestor(anna).created(CREATED).build();

        ItemRequestWithItemsDto dto = ItemRequestMapper.toItemRequestWithItemsDto(entity, List.of());

        assertThat(dto.getItems()).isEmpty();
    }
}
