package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.AbstractIntegrationTest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRequestRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Test
    void findAllByRequestorId_returnsOnlyOwnRequestsNewestFirst() {
        User anna = this.createUser("Анна", "anna@example.com");
        User boris = this.createUser("Борис", "boris@example.com");
        LocalDateTime now = LocalDateTime.now();
        this.createItemRequest("нужна дрель", anna, now.minusHours(2));
        this.createItemRequest("нужна пила", anna, now.minusHours(1));
        this.createItemRequest("нужен рубанок", boris, now);

        List<ItemRequest> found = this.itemRequestRepository
                .findAllByRequestorIdOrderByCreatedDesc(anna.getId());

        assertThat(found).hasSize(2);
        assertThat(found).extracting(ItemRequest::getDescription)
                .containsExactly("нужна пила", "нужна дрель");
    }

    @Test
    void findAllByRequestorIdNot_returnsOnlyOthersRequestsNewestFirst() {
        User anna = this.createUser("Анна", "anna@example.com");
        User boris = this.createUser("Борис", "boris@example.com");
        LocalDateTime now = LocalDateTime.now();
        this.createItemRequest("нужна дрель", anna, now.minusHours(2));
        this.createItemRequest("нужна пила", boris, now.minusHours(1));
        this.createItemRequest("нужен рубанок", boris, now);

        List<ItemRequest> found = this.itemRequestRepository
                .findAllByRequestorIdNotOrderByCreatedDesc(anna.getId());

        assertThat(found).extracting(ItemRequest::getDescription)
                .containsExactly("нужен рубанок", "нужна пила");
    }

    @Test
    void findAllByRequestorId_returnsEmptyListWhenNoRequests() {
        User anna = this.createUser("Анна", "anna@example.com");

        assertThat(this.itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(anna.getId()))
                .isEmpty();
    }

    @Test
    void findAllByRequestIdIn_collectsAnswersForSeveralRequests() {
        User anna = this.createUser("Анна", "anna@example.com");
        User boris = this.createUser("Борис", "boris@example.com");
        LocalDateTime now = LocalDateTime.now();
        ItemRequest drill = this.createItemRequest("нужна дрель", anna, now.minusHours(1));
        ItemRequest saw = this.createItemRequest("нужна пила", anna, now);
        this.createItemForRequest("Дрель Bosch", boris, drill.getId());
        this.createItemForRequest("Пила Makita", boris, saw.getId());
        this.createItem("Стремянка", "Без запроса", true, boris);

        assertThat(this.itemRepository.findAllByRequestIdIn(List.of(drill.getId(), saw.getId())))
                .hasSize(2);
        assertThat(this.itemRepository.findAllByRequestId(drill.getId()))
                .extracting(item -> item.getName())
                .containsExactly("Дрель Bosch");
    }
}
