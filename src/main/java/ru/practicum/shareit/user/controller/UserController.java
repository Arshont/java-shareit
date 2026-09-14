package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody NewUserRequest request) {
        return this.userService.create(request);
    }

    @GetMapping
    public List<UserDto> getAll() {
        return this.userService.getAll();
    }

    @GetMapping("/{userId}")
    public UserDto getById(@PathVariable Long userId) {
        return this.userService.getById(userId);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Long userId, @Valid @RequestBody UpdateUserRequest request) {
        return this.userService.update(userId, request);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId) {
        this.userService.delete(userId);
    }
}
