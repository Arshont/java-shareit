package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();

    private long lastId;

    @Override
    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(++this.lastId);
        }
        this.items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(this.items.get(id));
    }

    @Override
    public List<Item> findAllByOwnerId(Long ownerId) {
        return this.items.values().stream()
                .filter(item -> ownerId.equals(item.getOwnerId()))
                .toList();
    }

    @Override
    public List<Item> search(String text) {
        String query = text.toLowerCase();
        return this.items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> containsIgnoreCase(item.getName(), query)
                        || containsIgnoreCase(item.getDescription(), query))
                .toList();
    }

    private static boolean containsIgnoreCase(String source, String lowerCaseQuery) {
        return source != null && source.toLowerCase().contains(lowerCaseQuery);
    }
}
