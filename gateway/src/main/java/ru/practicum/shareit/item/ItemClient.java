package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {

    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build());
    }

    public ResponseEntity<Object> create(long userId, NewItemDto request) {
        return this.post("", userId, request);
    }

    public ResponseEntity<Object> update(long userId, long itemId, UpdateItemDto request) {
        return this.patch("/" + itemId, userId, request);
    }

    public ResponseEntity<Object> getById(long userId, long itemId) {
        return this.get("/" + itemId, userId);
    }

    public ResponseEntity<Object> getAllByOwner(long userId) {
        return this.get("", userId);
    }

    public ResponseEntity<Object> search(long userId, String text) {
        return this.get("/search?text={text}", userId, Map.of("text", text));
    }

    public ResponseEntity<Object> addComment(long userId, long itemId, NewCommentDto request) {
        return this.post("/" + itemId + "/comment", userId, request);
    }
}
