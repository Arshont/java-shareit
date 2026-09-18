package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {

    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build());
    }

    public ResponseEntity<Object> create(long userId, NewBookingDto request) {
        return this.post("", userId, request);
    }

    public ResponseEntity<Object> approve(long userId, long bookingId, boolean approved) {
        return this.patchNoBody("/" + bookingId + "?approved={approved}", userId,
                Map.of("approved", approved));
    }

    public ResponseEntity<Object> getById(long userId, long bookingId) {
        return this.get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getAllByBooker(long userId, BookingState state) {
        return this.get("?state={state}", userId, Map.of("state", state.name()));
    }

    public ResponseEntity<Object> getAllByOwner(long userId, BookingState state) {
        return this.get("/owner?state={state}", userId, Map.of("state", state.name()));
    }
}
