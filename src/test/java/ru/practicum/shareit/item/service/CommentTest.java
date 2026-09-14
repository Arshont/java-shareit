package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.AbstractIntegrationTest;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.NewCommentRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommentTest extends AbstractIntegrationTest {

    @Autowired
    private ItemService itemService;

    private User owner;
    private User booker;
    private Item item;
    private LocalDateTime now;

    @BeforeEach
    void setUpFixture() {
        this.owner = this.createUser("Владелец", "owner@example.com");
        this.booker = this.createUser("Арендатор", "booker@example.com");
        this.item = this.createItem("Дрель", "Ударная", true, this.owner);
        this.now = LocalDateTime.now();
    }

    @Test
    void addComment_afterFinishedApprovedBooking_succeeds() {
        this.createBooking(this.item, this.booker,
                this.now.minusDays(5), this.now.minusDays(4), BookingStatus.APPROVED);

        CommentDto created = this.itemService.addComment(
                this.booker.getId(), this.item.getId(), this.newComment("Отличная дрель"));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getText()).isEqualTo("Отличная дрель");
        assertThat(created.getAuthorName()).isEqualTo("Арендатор");
        assertThat(created.getCreated()).isNotNull();
    }

    @Test
    void addComment_withoutAnyBooking_throwsValidation() {
        assertThatThrownBy(() -> this.itemService.addComment(
                this.booker.getId(), this.item.getId(), this.newComment("Отличная дрель")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void addComment_whenBookingNotApproved_throwsValidation() {
        this.createBooking(this.item, this.booker,
                this.now.minusDays(5), this.now.minusDays(4), BookingStatus.WAITING);

        assertThatThrownBy(() -> this.itemService.addComment(
                this.booker.getId(), this.item.getId(), this.newComment("Отличная дрель")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void addComment_whenBookingNotFinished_throwsValidation() {
        this.createBooking(this.item, this.booker,
                this.now.plusDays(1), this.now.plusDays(2), BookingStatus.APPROVED);

        assertThatThrownBy(() -> this.itemService.addComment(
                this.booker.getId(), this.item.getId(), this.newComment("Отличная дрель")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void addComment_byUnknownUser_throwsNotFound() {
        assertThatThrownBy(() -> this.itemService.addComment(
                9999L, this.item.getId(), this.newComment("Отличная дрель")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addComment_toUnknownItem_throwsNotFound() {
        assertThatThrownBy(() -> this.itemService.addComment(
                this.booker.getId(), 9999L, this.newComment("Отличная дрель")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addComment_whenBookingBelongsToAnotherUser_throwsValidation() {
        User stranger = this.createUser("Посторонний", "stranger@example.com");
        this.createBooking(this.item, this.booker,
                this.now.minusDays(5), this.now.minusDays(4), BookingStatus.APPROVED);

        assertThatThrownBy(() -> this.itemService.addComment(
                stranger.getId(), this.item.getId(), this.newComment("Отличная дрель")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void addComment_whenBookingIsForAnotherItem_throwsValidation() {
        Item another = this.createItem("Пила", "Циркулярная", true, this.owner);
        this.createBooking(another, this.booker,
                this.now.minusDays(5), this.now.minusDays(4), BookingStatus.APPROVED);

        assertThatThrownBy(() -> this.itemService.addComment(
                this.booker.getId(), this.item.getId(), this.newComment("Отличная дрель")))
                .isInstanceOf(ValidationException.class);
    }

    private NewCommentRequest newComment(String text) {
        NewCommentRequest request = new NewCommentRequest();
        request.setText(text);
        return request;
    }
}
