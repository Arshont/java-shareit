package ru.practicum.shareit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

@SpringBootTest
@Transactional
public abstract class AbstractIntegrationTest {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ItemRepository itemRepository;

    @Autowired
    protected BookingRepository bookingRepository;

    @Autowired
    protected ItemRequestRepository itemRequestRepository;

    protected User createUser(String name, String email) {
        return this.userRepository.save(User.builder().name(name).email(email).build());
    }

    protected Item createItem(String name, String description, boolean available, User owner) {
        return this.itemRepository.save(Item.builder()
                .name(name)
                .description(description)
                .available(available)
                .owner(owner)
                .build());
    }

    protected ItemRequest createItemRequest(String description, User requestor, LocalDateTime created) {
        return this.itemRequestRepository.save(ItemRequest.builder()
                .description(description)
                .requestor(requestor)
                .created(created)
                .build());
    }

    protected Item createItemForRequest(String name, User owner, Long requestId) {
        return this.itemRepository.save(Item.builder()
                .name(name)
                .description("Ответ на запрос")
                .available(true)
                .owner(owner)
                .requestId(requestId)
                .build());
    }

    protected Booking createBooking(Item item, User booker, LocalDateTime start,
                                    LocalDateTime end, BookingStatus status) {
        return this.bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(start)
                .end(end)
                .status(status)
                .build());
    }
}
