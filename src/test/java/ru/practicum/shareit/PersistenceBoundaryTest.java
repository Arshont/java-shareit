package ru.practicum.shareit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PersistenceBoundaryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @AfterEach
    void cleanUp() {
        this.commentRepository.deleteAll();
        this.bookingRepository.deleteAll();
        this.itemRepository.deleteAll();
        this.userRepository.deleteAll();
    }

    @Test
    void bookingDtoIsFullyPopulatedWithoutSurroundingTransaction() {
        User owner = this.userRepository.save(
                User.builder().name("Владелец").email("boundary-owner@example.com").build());
        User booker = this.userRepository.save(
                User.builder().name("Арендатор").email("boundary-booker@example.com").build());
        Item item = this.itemRepository.save(Item.builder()
                .name("Дрель")
                .description("Ударная")
                .available(true)
                .owner(owner)
                .build());
        LocalDateTime now = LocalDateTime.now().withNano(0);
        this.bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .status(BookingStatus.WAITING)
                .build());

        List<BookingDto> found = this.bookingService.getAllByBooker(
                booker.getId(), ru.practicum.shareit.booking.dto.BookingState.ALL);

        assertThat(found).hasSize(1);
        BookingDto dto = found.get(0);
        assertThat(dto.getBooker()).isNotNull();
        assertThat(dto.getBooker().getName()).isEqualTo("Арендатор");
        assertThat(dto.getItem()).isNotNull();
        assertThat(dto.getItem().getName()).isEqualTo("Дрель");
    }

    @Test
    void deletingUserCascadesToItemsBookingsAndComments() {
        User owner = this.userRepository.save(
                User.builder().name("Владелец").email("cascade-owner@example.com").build());
        User booker = this.userRepository.save(
                User.builder().name("Арендатор").email("cascade-booker@example.com").build());
        Item item = this.itemRepository.save(Item.builder()
                .name("Дрель")
                .description("Ударная")
                .available(true)
                .owner(owner)
                .build());
        LocalDateTime now = LocalDateTime.now().withNano(0);
        this.bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(now.minusDays(5))
                .end(now.minusDays(4))
                .status(BookingStatus.APPROVED)
                .build());
        this.commentRepository.save(Comment.builder()
                .text("Отличная дрель")
                .item(item)
                .author(booker)
                .created(now.minusDays(3))
                .build());

        this.userService.delete(owner.getId());

        assertThat(this.itemRepository.count()).isZero();
        assertThat(this.bookingRepository.count()).isZero();
        assertThat(this.commentRepository.count()).isZero();
        assertThat(this.userRepository.count()).isEqualTo(1);
    }
}
