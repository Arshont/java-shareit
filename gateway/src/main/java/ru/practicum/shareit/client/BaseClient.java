package ru.practicum.shareit.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.common.ShareItHeaders;

import java.util.List;
import java.util.Map;

public class BaseClient {

    protected final RestTemplate rest;

    public BaseClient(RestTemplate rest) {
        this.rest = rest;
    }

    protected ResponseEntity<Object> get(String path) {
        return this.get(path, null, null);
    }

    protected ResponseEntity<Object> get(String path, long userId) {
        return this.get(path, userId, null);
    }

    protected ResponseEntity<Object> get(String path, Long userId, @Nullable Map<String, Object> parameters) {
        return this.makeAndSendRequest(HttpMethod.GET, path, userId, parameters, null);
    }

    protected <T> ResponseEntity<Object> post(String path, T body) {
        return this.post(path, null, null, body);
    }

    protected <T> ResponseEntity<Object> post(String path, long userId, T body) {
        return this.post(path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> post(String path, Long userId,
                                              @Nullable Map<String, Object> parameters, T body) {
        return this.makeAndSendRequest(HttpMethod.POST, path, userId, parameters, body);
    }

    protected <T> ResponseEntity<Object> patch(String path, T body) {
        return this.patch(path, null, null, body);
    }

    protected <T> ResponseEntity<Object> patch(String path, long userId, T body) {
        return this.patch(path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> patch(String path, Long userId,
                                               @Nullable Map<String, Object> parameters, T body) {
        return this.makeAndSendRequest(HttpMethod.PATCH, path, userId, parameters, body);
    }

    protected ResponseEntity<Object> patchNoBody(String path, long userId,
                                                 @Nullable Map<String, Object> parameters) {
        return this.makeAndSendRequest(HttpMethod.PATCH, path, userId, parameters, null);
    }

    protected ResponseEntity<Object> delete(String path) {
        return this.delete(path, null, null);
    }

    protected ResponseEntity<Object> delete(String path, long userId) {
        return this.delete(path, userId, null);
    }

    protected ResponseEntity<Object> delete(String path, Long userId,
                                            @Nullable Map<String, Object> parameters) {
        return this.makeAndSendRequest(HttpMethod.DELETE, path, userId, parameters, null);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method, String path, Long userId,
                                                          @Nullable Map<String, Object> parameters,
                                                          @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders(userId));

        if (parameters != null) {
            return this.rest.exchange(path, method, requestEntity, Object.class, parameters);
        }
        return this.rest.exchange(path, method, requestEntity, Object.class);
    }

    private static HttpHeaders defaultHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (userId != null) {
            headers.set(ShareItHeaders.USER_ID, String.valueOf(userId));
        }
        return headers;
    }
}
