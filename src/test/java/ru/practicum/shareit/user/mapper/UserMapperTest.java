package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    void toUser_copiesNameAndEmail() {
        NewUserRequest request = new NewUserRequest();
        request.setName("Иван");
        request.setEmail("ivan@example.com");

        User user = UserMapper.toUser(request);

        assertThat(user.getId()).isNull();
        assertThat(user.getName()).isEqualTo("Иван");
        assertThat(user.getEmail()).isEqualTo("ivan@example.com");
    }

    @Test
    void toUserDto_copiesAllFields() {
        User user = User.builder()
                .id(7L)
                .name("Иван")
                .email("ivan@example.com")
                .build();

        UserDto dto = UserMapper.toUserDto(user);

        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getName()).isEqualTo("Иван");
        assertThat(dto.getEmail()).isEqualTo("ivan@example.com");
    }

    @Test
    void updateUserFields_withOnlyName_keepsEmail() {
        User existing = this.existingUser();
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Новое имя");

        UserMapper.updateUserFields(existing, request);

        assertThat(existing.getName()).isEqualTo("Новое имя");
        assertThat(existing.getEmail()).isEqualTo("old@example.com");
    }

    @Test
    void updateUserFields_withOnlyEmail_keepsName() {
        User existing = this.existingUser();
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("new@example.com");

        UserMapper.updateUserFields(existing, request);

        assertThat(existing.getName()).isEqualTo("Старое имя");
        assertThat(existing.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void updateUserFields_withBlankValues_keepsBothFields() {
        User existing = this.existingUser();
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("   ");
        request.setEmail("");

        UserMapper.updateUserFields(existing, request);

        assertThat(existing.getName()).isEqualTo("Старое имя");
        assertThat(existing.getEmail()).isEqualTo("old@example.com");
    }

    private User existingUser() {
        return User.builder()
                .id(1L)
                .name("Старое имя")
                .email("old@example.com")
                .build();
    }
}
