package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.repository.InMemoryItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.InMemoryUserRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemServiceImplTest {

    private ItemService itemService;
    private UserRepository userRepository;

    private Long ownerId;
    private Long strangerId;

    @BeforeEach
    void setUp() {
        this.userRepository = new InMemoryUserRepository();
        this.itemService = new ItemServiceImpl(new InMemoryItemRepository(), this.userRepository);
        this.ownerId = this.createUser("Владелец", "owner@example.com");
        this.strangerId = this.createUser("Посторонний", "stranger@example.com");
    }

    @Test
    void create_assignsIdAndKeepsFields() {
        ItemDto created = this.itemService.create(this.ownerId, this.newItem("Дрель", "Ударная", true));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Дрель");
        assertThat(created.getDescription()).isEqualTo("Ударная");
        assertThat(created.getAvailable()).isTrue();
    }

    @Test
    void create_withUnknownOwner_throwsNotFound() {
        assertThatThrownBy(() -> this.itemService.create(999L, this.newItem("Дрель", "Ударная", true)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_byUnknownUser_throwsNotFound() {
        ItemDto created = this.itemService.create(this.ownerId, this.newItem("Дрель", "Ударная", true));
        UpdateItemRequest request = new UpdateItemRequest();
        request.setName("Перфоратор");

        assertThatThrownBy(() -> this.itemService.update(999L, created.getId(), request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_byExistingNonOwner_throwsForbidden() {
        ItemDto created = this.itemService.create(this.ownerId, this.newItem("Дрель", "Ударная", true));
        UpdateItemRequest request = new UpdateItemRequest();
        request.setName("Перфоратор");

        assertThatThrownBy(() -> this.itemService.update(this.strangerId, created.getId(), request))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void update_whenItemAbsent_throwsNotFound() {
        UpdateItemRequest request = new UpdateItemRequest();
        request.setName("Перфоратор");

        assertThatThrownBy(() -> this.itemService.update(this.ownerId, 999L, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_withOnlyAvailable_keepsNameAndDescription() {
        ItemDto created = this.itemService.create(this.ownerId, this.newItem("Дрель", "Ударная", true));
        UpdateItemRequest request = new UpdateItemRequest();
        request.setAvailable(false);

        ItemDto updated = this.itemService.update(this.ownerId, created.getId(), request);

        assertThat(updated.getAvailable()).isFalse();
        assertThat(updated.getName()).isEqualTo("Дрель");
        assertThat(updated.getDescription()).isEqualTo("Ударная");
    }

    @Test
    void update_withOnlyName_keepsDescriptionAndAvailable() {
        ItemDto created = this.itemService.create(this.ownerId, this.newItem("Дрель", "Ударная", true));
        UpdateItemRequest request = new UpdateItemRequest();
        request.setName("Перфоратор");

        ItemDto updated = this.itemService.update(this.ownerId, created.getId(), request);

        assertThat(updated.getName()).isEqualTo("Перфоратор");
        assertThat(updated.getDescription()).isEqualTo("Ударная");
        assertThat(updated.getAvailable()).isTrue();
    }

    @Test
    void getById_whenAbsent_throwsNotFound() {
        assertThatThrownBy(() -> this.itemService.getById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_returnsCreatedItem() {
        ItemDto created = this.itemService.create(this.ownerId, this.newItem("Дрель", "Ударная", true));

        ItemDto found = this.itemService.getById(created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getName()).isEqualTo("Дрель");
        assertThat(found.getDescription()).isEqualTo("Ударная");
        assertThat(found.getAvailable()).isTrue();
    }

    @Test
    void getById_byNonOwner_returnsItem() {
        ItemDto created = this.itemService.create(this.ownerId, this.newItem("Дрель", "Ударная", true));

        ItemDto found = this.itemService.getById(created.getId());

        assertThat(found.getName()).isEqualTo("Дрель");
    }

    @Test
    void getAllByOwner_returnsOnlyOwnItems() {
        this.itemService.create(this.ownerId, this.newItem("Дрель", "Ударная", true));
        this.itemService.create(this.ownerId, this.newItem("Пила", "Циркулярная", true));
        this.itemService.create(this.strangerId, this.newItem("Чужая", "Описание", true));

        assertThat(this.itemService.getAllByOwner(this.ownerId))
                .extracting(ItemDto::getName)
                .containsExactlyInAnyOrder("Дрель", "Пила");
    }

    @Test
    void getAllByOwner_withUnknownUser_throwsNotFound() {
        assertThatThrownBy(() -> this.itemService.getAllByOwner(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllByOwner_whenOwnerHasNoItems_returnsEmptyList() {
        assertThat(this.itemService.getAllByOwner(this.ownerId)).isEmpty();
    }

    @Test
    void search_findsByNameIgnoringCase() {
        this.itemService.create(this.ownerId, this.newItem("Дрель", "Ударная", true));

        assertThat(this.itemService.search("дРеЛь"))
                .extracting(ItemDto::getName)
                .containsExactly("Дрель");
    }

    @Test
    void search_findsByDescription() {
        this.itemService.create(this.ownerId, this.newItem("Инструмент", "Ударная дрель", true));

        assertThat(this.itemService.search("дрель"))
                .extracting(ItemDto::getName)
                .containsExactly("Инструмент");
    }

    @Test
    void search_skipsUnavailableItems() {
        this.itemService.create(this.ownerId, this.newItem("Дрель", "Ударная", false));

        assertThat(this.itemService.search("дрель")).isEmpty();
    }

    @Test
    void search_withNullText_returnsEmptyList() {
        this.itemService.create(this.ownerId, this.newItem("Дрель", "Ударная", true));

        assertThat(this.itemService.search(null)).isEmpty();
    }

    @Test
    void search_withEmptyText_returnsEmptyList() {
        this.itemService.create(this.ownerId, this.newItem("Дрель", "Ударная", true));

        assertThat(this.itemService.search("")).isEmpty();
    }

    @Test
    void search_withBlankText_returnsEmptyList() {
        this.itemService.create(this.ownerId, this.newItem("Дрель", "Ударная", true));

        assertThat(this.itemService.search("   ")).isEmpty();
    }

    @Test
    void search_returnsItemsOfAllOwners() {
        this.itemService.create(this.ownerId, this.newItem("Дрель владельца", "Ударная", true));
        this.itemService.create(this.strangerId, this.newItem("Дрель постороннего", "Ударная", true));

        assertThat(this.itemService.search("дрель"))
                .extracting(ItemDto::getName)
                .containsExactlyInAnyOrder("Дрель владельца", "Дрель постороннего");
    }

    private NewItemRequest newItem(String name, String description, boolean available) {
        NewItemRequest request = new NewItemRequest();
        request.setName(name);
        request.setDescription(description);
        request.setAvailable(available);
        return request;
    }

    private Long createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return this.userRepository.save(user).getId();
    }
}
