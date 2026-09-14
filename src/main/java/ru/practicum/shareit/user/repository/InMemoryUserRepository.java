package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();

    private long lastId;

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(++this.lastId);
        }
        this.users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(this.users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(this.users.values());
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return this.users.values().stream()
                .filter(user -> user.getEmail() != null && user.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public void deleteById(Long id) {
        this.users.remove(id);
    }
}
