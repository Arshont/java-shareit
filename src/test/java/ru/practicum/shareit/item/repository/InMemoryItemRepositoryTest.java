package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryItemRepositoryTest {

    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        this.itemRepository = new InMemoryItemRepository();
    }

    @Test
    void save_assignsSequentialIdsStartingFromOne() {
        Item first = this.itemRepository.save(this.item("Дрель", "Ударная", true, 1L));
        Item second = this.itemRepository.save(this.item("Пила", "Циркулярная", true, 1L));

        assertThat(first.getId()).isEqualTo(1L);
        assertThat(second.getId()).isEqualTo(2L);
    }

    @Test
    void findById_whenAbsent_returnsEmpty() {
        assertThat(this.itemRepository.findById(42L)).isEmpty();
    }

    @Test
    void findAllByOwnerId_returnsOnlyOwnItems() {
        this.itemRepository.save(this.item("Дрель", "Ударная", true, 1L));
        this.itemRepository.save(this.item("Пила", "Циркулярная", true, 1L));
        this.itemRepository.save(this.item("Чужая вещь", "Описание", true, 2L));

        assertThat(this.itemRepository.findAllByOwnerId(1L))
                .extracting(Item::getName)
                .containsExactlyInAnyOrder("Дрель", "Пила");
    }

    @Test
    void search_findsByNameIgnoringCase() {
        this.itemRepository.save(this.item("Дрель", "Ударная", true, 1L));

        assertThat(this.itemRepository.search("дРеЛь"))
                .extracting(Item::getName)
                .containsExactly("Дрель");
    }

    @Test
    void search_findsByDescriptionIgnoringCase() {
        this.itemRepository.save(this.item("Инструмент", "Ударная дрель", true, 1L));

        assertThat(this.itemRepository.search("УДАРНАЯ"))
                .extracting(Item::getName)
                .containsExactly("Инструмент");
    }

    @Test
    void search_skipsUnavailableItems() {
        this.itemRepository.save(this.item("Дрель", "Ударная", false, 1L));

        assertThat(this.itemRepository.search("дрель")).isEmpty();
    }

    @Test
    void search_whenNothingMatches_returnsEmptyList() {
        this.itemRepository.save(this.item("Дрель", "Ударная", true, 1L));

        assertThat(this.itemRepository.search("велосипед")).isEmpty();
    }

    private Item item(String name, String description, boolean available, Long ownerId) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwnerId(ownerId);
        return item;
    }
}
