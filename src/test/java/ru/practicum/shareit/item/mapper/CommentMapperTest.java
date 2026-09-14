package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {

    @Test
    void toCommentDto_flattensAuthorName() {
        LocalDateTime created = LocalDateTime.of(2026, 10, 1, 12, 0, 0);
        User owner = User.builder().id(1L).name("Владелец").email("owner@example.com").build();
        User author = User.builder().id(2L).name("Иван").email("ivan@example.com").build();
        Item item = Item.builder()
                .id(3L)
                .name("Дрель")
                .description("Ударная")
                .available(true)
                .owner(owner)
                .build();
        Comment comment = Comment.builder()
                .id(7L)
                .text("Отличная дрель")
                .item(item)
                .author(author)
                .created(created)
                .build();

        CommentDto dto = CommentMapper.toCommentDto(comment);

        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getText()).isEqualTo("Отличная дрель");
        assertThat(dto.getAuthorName()).isEqualTo("Иван");
        assertThat(dto.getCreated()).isEqualTo(created);
    }
}
