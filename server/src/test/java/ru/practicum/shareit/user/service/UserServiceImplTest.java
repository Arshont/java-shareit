package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.AbstractIntegrationTest;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceImplTest extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void create_assignsIdAndKeepsFields() {
        UserDto created = this.userService.create(this.newUser("Иван", "ivan@example.com"));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Иван");
        assertThat(created.getEmail()).isEqualTo("ivan@example.com");
    }

    @Test
    void create_withTakenEmail_throwsConflict() {
        this.userService.create(this.newUser("Иван", "ivan@example.com"));

        assertThatThrownBy(() -> this.userService.create(this.newUser("Пётр", "ivan@example.com")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void create_withTakenEmailInOtherCase_throwsConflict() {
        this.userService.create(this.newUser("Иван", "ivan@example.com"));

        assertThatThrownBy(() -> this.userService.create(this.newUser("Пётр", "IVAN@EXAMPLE.COM")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void update_withOnlyName_keepsEmail() {
        UserDto created = this.userService.create(this.newUser("Иван", "ivan@example.com"));
        UpdateUserDto request = new UpdateUserDto();
        request.setName("Иван Иванович");

        UserDto updated = this.userService.update(created.getId(), request);

        assertThat(updated.getName()).isEqualTo("Иван Иванович");
        assertThat(updated.getEmail()).isEqualTo("ivan@example.com");
    }

    @Test
    void update_withOnlyEmail_keepsName() {
        UserDto created = this.userService.create(this.newUser("Иван", "ivan@example.com"));
        UpdateUserDto request = new UpdateUserDto();
        request.setEmail("new@example.com");

        UserDto updated = this.userService.update(created.getId(), request);

        assertThat(updated.getName()).isEqualTo("Иван");
        assertThat(updated.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void update_withEmailOfAnotherUser_throwsConflict() {
        this.userService.create(this.newUser("Иван", "ivan@example.com"));
        UserDto second = this.userService.create(this.newUser("Пётр", "petr@example.com"));
        UpdateUserDto request = new UpdateUserDto();
        request.setEmail("ivan@example.com");

        assertThatThrownBy(() -> this.userService.update(second.getId(), request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void update_withOwnUnchangedEmail_succeeds() {
        UserDto created = this.userService.create(this.newUser("Иван", "ivan@example.com"));
        UpdateUserDto request = new UpdateUserDto();
        request.setName("Иван Иванович");
        request.setEmail("ivan@example.com");

        UserDto updated = this.userService.update(created.getId(), request);

        assertThat(updated.getName()).isEqualTo("Иван Иванович");
        assertThat(updated.getEmail()).isEqualTo("ivan@example.com");
    }

    @Test
    void update_whenUserAbsent_throwsNotFound() {
        UpdateUserDto request = new UpdateUserDto();
        request.setName("Кто-то");

        assertThatThrownBy(() -> this.userService.update(9999L, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_returnsCreatedUser() {
        UserDto created = this.userService.create(this.newUser("Иван", "ivan@example.com"));

        UserDto found = this.userService.getById(created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getName()).isEqualTo("Иван");
        assertThat(found.getEmail()).isEqualTo("ivan@example.com");
    }

    @Test
    void getById_whenAbsent_throwsNotFound() {
        assertThatThrownBy(() -> this.userService.getById(9999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_removesUser() {
        UserDto created = this.userService.create(this.newUser("Иван", "ivan@example.com"));

        this.userService.delete(created.getId());

        assertThatThrownBy(() -> this.userService.getById(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_whenUserAbsent_throwsNotFound() {
        assertThatThrownBy(() -> this.userService.delete(9999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAll_returnsAllUsers() {
        this.userService.create(this.newUser("Анна", "anna@example.com"));
        this.userService.create(this.newUser("Борис", "boris@example.com"));

        List<UserDto> all = this.userService.getAll();

        assertThat(all).extracting(UserDto::getEmail)
                .containsExactlyInAnyOrder("anna@example.com", "boris@example.com");
    }

    private NewUserDto newUser(String name, String email) {
        NewUserDto request = new NewUserDto();
        request.setName(name);
        request.setEmail(email);
        return request;
    }
}
