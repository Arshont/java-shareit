package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto create(NewUserRequest request) {
        this.ensureEmailIsFree(request.getEmail());
        User saved = this.userRepository.save(UserMapper.toUser(request));
        log.info("Создан пользователь id={}", saved.getId());
        return UserMapper.toUserDto(saved);
    }

    @Override
    public UserDto update(Long userId, UpdateUserRequest request) {
        User existing = this.findUserOrThrow(userId);
        if (request.hasEmail()) {
            this.ensureEmailIsFree(request.getEmail(), userId);
        }
        UserMapper.updateUserFields(existing, request);
        User saved = this.userRepository.save(existing);
        log.info("Обновлён пользователь id={}", saved.getId());
        return UserMapper.toUserDto(saved);
    }

    @Override
    public UserDto getById(Long userId) {
        return UserMapper.toUserDto(this.findUserOrThrow(userId));
    }

    @Override
    public List<UserDto> getAll() {
        return this.userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public void delete(Long userId) {
        this.findUserOrThrow(userId);
        this.userRepository.deleteById(userId);
        log.info("Удалён пользователь id={}", userId);
    }

    private User findUserOrThrow(Long userId) {
        return this.userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.user(userId));
    }

    private void ensureEmailIsFree(String email) {
        this.ensureEmailIsFree(email, null);
    }

    private void ensureEmailIsFree(String email, Long excludedUserId) {
        Optional<User> owner = this.userRepository.findByEmail(email);
        if (owner.isPresent() && !owner.get().getId().equals(excludedUserId)) {
            throw new ConflictException("Email %s уже используется".formatted(email));
        }
    }
}
