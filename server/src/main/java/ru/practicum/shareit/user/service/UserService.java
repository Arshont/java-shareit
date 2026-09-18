package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto create(NewUserDto request);

    UserDto update(Long userId, UpdateUserDto request);

    UserDto getById(Long userId);

    List<UserDto> getAll();

    void delete(Long userId);
}
