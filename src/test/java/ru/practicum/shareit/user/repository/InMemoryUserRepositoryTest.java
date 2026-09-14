package ru.practicum.shareit.user.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryUserRepositoryTest {

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        this.userRepository = new InMemoryUserRepository();
    }

    @Test
    void save_assignsSequentialIdsStartingFromOne() {
        User first = this.userRepository.save(this.user("Иван", "ivan@example.com"));
        User second = this.userRepository.save(this.user("Пётр", "petr@example.com"));

        assertThat(first.getId()).isEqualTo(1L);
        assertThat(second.getId()).isEqualTo(2L);
    }

    @Test
    void save_withExistingId_keepsIdAndOverwritesEntry() {
        User saved = this.userRepository.save(this.user("Иван", "ivan@example.com"));
        saved.setName("Иван Иванович");

        User updated = this.userRepository.save(saved);

        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(this.userRepository.findAll()).hasSize(1);
        assertThat(this.userRepository.findById(saved.getId()))
                .get()
                .extracting(User::getName)
                .isEqualTo("Иван Иванович");
    }

    @Test
    void findById_whenAbsent_returnsEmpty() {
        assertThat(this.userRepository.findById(42L)).isEmpty();
    }

    @Test
    void findByEmail_ignoresCase() {
        User saved = this.userRepository.save(this.user("Иван", "ivan@example.com"));

        Optional<User> found = this.userRepository.findByEmail("IVAN@EXAMPLE.COM");

        assertThat(found).get().extracting(User::getId).isEqualTo(saved.getId());
    }

    @Test
    void findByEmail_whenAbsent_returnsEmpty() {
        this.userRepository.save(this.user("Иван", "ivan@example.com"));

        assertThat(this.userRepository.findByEmail("petr@example.com")).isEmpty();
    }

    @Test
    void findAll_returnsAllSavedUsers() {
        this.userRepository.save(this.user("Иван", "ivan@example.com"));
        this.userRepository.save(this.user("Пётр", "petr@example.com"));

        assertThat(this.userRepository.findAll())
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder("ivan@example.com", "petr@example.com");
    }

    @Test
    void deleteById_removesUser() {
        User saved = this.userRepository.save(this.user("Иван", "ivan@example.com"));

        this.userRepository.deleteById(saved.getId());

        assertThat(this.userRepository.findById(saved.getId())).isEmpty();
        assertThat(this.userRepository.findAll()).isEmpty();
    }

    private User user(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}
