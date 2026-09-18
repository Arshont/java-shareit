package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.AbstractIntegrationTest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemAnswerDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemRequestServiceImplTest extends AbstractIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    private static NewItemRequestDto newRequest(String description) {
        NewItemRequestDto request = new NewItemRequestDto();
        request.setDescription(description);
        return request;
    }

    @Test
    void create_savesRequestWithCreatedTimestamp() {
        User anna = this.createUser("Анна", "anna@example.com");
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        ItemRequestDto created = this.itemRequestService.create(anna.getId(), newRequest("нужна дрель"));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("нужна дрель");
        assertThat(created.getCreated()).isAfter(before);
    }

    @Test
    void create_withUnknownUser_throwsNotFound() {
        assertThatThrownBy(() -> this.itemRequestService.create(9999L, newRequest("нужна дрель")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getOwn_returnsOnlyOwnRequestsNewestFirst() {
        User anna = this.createUser("Анна", "anna@example.com");
        User boris = this.createUser("Борис", "boris@example.com");
        LocalDateTime now = LocalDateTime.now();
        this.createItemRequest("нужна дрель", anna, now.minusHours(2));
        this.createItemRequest("нужна пила", anna, now.minusHours(1));
        this.createItemRequest("нужен рубанок", boris, now);

        List<ItemRequestWithItemsDto> own = this.itemRequestService.getOwn(anna.getId());

        assertThat(own).extracting(ItemRequestWithItemsDto::getDescription)
                .containsExactly("нужна пила", "нужна дрель");
    }

    @Test
    void getOwn_fillsAnswersForEachRequest() {
        User anna = this.createUser("Анна", "anna@example.com");
        User boris = this.createUser("Борис", "boris@example.com");
        LocalDateTime now = LocalDateTime.now();
        ItemRequest drill = this.createItemRequest("нужна дрель", anna, now.minusHours(1));
        this.createItemRequest("нужна пила", anna, now);
        this.createItemForRequest("Дрель Bosch", boris, drill.getId());

        List<ItemRequestWithItemsDto> own = this.itemRequestService.getOwn(anna.getId());

        ItemRequestWithItemsDto sawRequest = own.get(0);
        ItemRequestWithItemsDto drillRequest = own.get(1);
        assertThat(sawRequest.getItems()).isEmpty();
        assertThat(drillRequest.getItems()).hasSize(1);
        assertThat(drillRequest.getItems().get(0).getName()).isEqualTo("Дрель Bosch");
        assertThat(drillRequest.getItems().get(0).getOwnerId()).isEqualTo(boris.getId());
    }

    @Test
    void getOwn_doesNotMixAnswersBetweenRequests() {
        User anna = this.createUser("Анна", "anna@example.com");
        User boris = this.createUser("Борис", "boris@example.com");
        LocalDateTime now = LocalDateTime.now();
        ItemRequest drill = this.createItemRequest("нужна дрель", anna, now.minusHours(1));
        ItemRequest saw = this.createItemRequest("нужна пила", anna, now);
        this.createItemForRequest("Дрель Bosch", boris, drill.getId());
        this.createItemForRequest("Пила Makita", boris, saw.getId());

        List<ItemRequestWithItemsDto> own = this.itemRequestService.getOwn(anna.getId());

        assertThat(own).hasSize(2);
        assertThat(own.get(0).getDescription()).isEqualTo("нужна пила");
        assertThat(own.get(0).getItems()).extracting(ItemAnswerDto::getName)
                .containsExactly("Пила Makita");
        assertThat(own.get(1).getDescription()).isEqualTo("нужна дрель");
        assertThat(own.get(1).getItems()).extracting(ItemAnswerDto::getName)
                .containsExactly("Дрель Bosch");
    }

    @Test
    void getOwn_withUnknownUser_throwsNotFound() {
        assertThatThrownBy(() -> this.itemRequestService.getOwn(9999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getOwn_withoutRequests_returnsEmptyList() {
        User anna = this.createUser("Анна", "anna@example.com");

        assertThat(this.itemRequestService.getOwn(anna.getId())).isEmpty();
    }

    @Test
    void getAll_returnsOthersRequestsNewestFirstAndExcludesOwn() {
        User anna = this.createUser("Анна", "anna@example.com");
        User boris = this.createUser("Борис", "boris@example.com");
        LocalDateTime now = LocalDateTime.now();
        this.createItemRequest("нужна дрель", anna, now.minusHours(2));
        this.createItemRequest("нужна пила", boris, now.minusHours(1));
        this.createItemRequest("нужен рубанок", boris, now);

        List<ItemRequestDto> all = this.itemRequestService.getAll(anna.getId());

        assertThat(all).extracting(ItemRequestDto::getDescription)
                .containsExactly("нужен рубанок", "нужна пила");
    }

    @Test
    void getAll_withUnknownUser_throwsNotFound() {
        assertThatThrownBy(() -> this.itemRequestService.getAll(9999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_isAvailableToAnyUserAndContainsAnswers() {
        User anna = this.createUser("Анна", "anna@example.com");
        User boris = this.createUser("Борис", "boris@example.com");
        User viktor = this.createUser("Виктор", "viktor@example.com");
        ItemRequest drill = this.createItemRequest("нужна дрель", anna, LocalDateTime.now());
        this.createItemForRequest("Дрель Bosch", boris, drill.getId());

        ItemRequestWithItemsDto found = this.itemRequestService.getById(viktor.getId(), drill.getId());

        assertThat(found.getDescription()).isEqualTo("нужна дрель");
        assertThat(found.getItems()).hasSize(1);
        assertThat(found.getItems().get(0).getOwnerId()).isEqualTo(boris.getId());
    }

    @Test
    void getById_withUnknownRequest_throwsNotFound() {
        User anna = this.createUser("Анна", "anna@example.com");

        assertThatThrownBy(() -> this.itemRequestService.getById(anna.getId(), 9999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_withUnknownUser_throwsNotFound() {
        User anna = this.createUser("Анна", "anna@example.com");
        ItemRequest drill = this.createItemRequest("нужна дрель", anna, LocalDateTime.now());

        assertThatThrownBy(() -> this.itemRequestService.getById(9999L, drill.getId()))
                .isInstanceOf(NotFoundException.class);
    }
}
