package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.AbstractIntegrationTest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommentRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void findAllByItemId_returnsCommentsOfThatItemOnly() {
        User owner = this.createUser("Владелец", "owner@example.com");
        User author = this.createUser("Иван", "ivan@example.com");
        Item first = this.createItem("Дрель", "Ударная", true, owner);
        Item second = this.createItem("Пила", "Циркулярная", true, owner);
        Comment target = this.saveComment("Про дрель", first, author);
        this.saveComment("Про пилу", second, author);

        List<Comment> found = this.commentRepository.findAllByItemIdOrderByCreatedDesc(first.getId());

        assertThat(found).extracting(Comment::getId).containsExactly(target.getId());
    }

    @Test
    void findAllByItemId_returnsNewestFirst() {
        User owner = this.createUser("Владелец", "owner@example.com");
        User author = this.createUser("Иван", "ivan@example.com");
        Item item = this.createItem("Дрель", "Ударная", true, owner);
        LocalDateTime now = LocalDateTime.now().withNano(0);
        Comment older = this.saveComment("Старый", item, author, now.minusHours(2));
        Comment newer = this.saveComment("Новый", item, author, now.minusHours(1));

        List<Comment> found = this.commentRepository.findAllByItemIdOrderByCreatedDesc(item.getId());

        assertThat(found).extracting(Comment::getId).containsExactly(newer.getId(), older.getId());
    }

    @Test
    void findAllByItemIdIn_returnsCommentsOfSeveralItemsNewestFirst() {
        User owner = this.createUser("Владелец", "owner@example.com");
        User author = this.createUser("Иван", "ivan@example.com");
        Item first = this.createItem("Дрель", "Ударная", true, owner);
        Item second = this.createItem("Пила", "Циркулярная", true, owner);
        LocalDateTime now = LocalDateTime.now().withNano(0);
        Comment aboutDrill = this.saveComment("Про дрель", first, author, now.minusHours(2));
        Comment aboutSaw = this.saveComment("Про пилу", second, author, now.minusHours(1));

        List<Comment> found = this.commentRepository
                .findAllByItemIdInOrderByCreatedDesc(List.of(first.getId(), second.getId()));

        assertThat(found).extracting(Comment::getId)
                .containsExactly(aboutSaw.getId(), aboutDrill.getId());
    }

    private Comment saveComment(String text, Item item, User author) {
        return this.saveComment(text, item, author, LocalDateTime.now().withNano(0));
    }

    private Comment saveComment(String text, Item item, User author, LocalDateTime created) {
        return this.commentRepository.save(Comment.builder()
                .text(text)
                .item(item)
                .author(author)
                .created(created)
                .build());
    }
}
