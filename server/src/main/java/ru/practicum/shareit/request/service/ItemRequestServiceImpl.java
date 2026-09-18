package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemAnswerDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, NewItemRequestDto request) {
        User requestor = this.findUserOrThrow(userId);
        ItemRequest saved = this.itemRequestRepository.save(
                ItemRequestMapper.toItemRequest(request, requestor, LocalDateTime.now()));
        log.info("Создан запрос вещи id={} пользователем id={}", saved.getId(), userId);
        return ItemRequestMapper.toItemRequestDto(saved);
    }

    @Override
    public List<ItemRequestWithItemsDto> getOwn(Long userId) {
        this.findUserOrThrow(userId);
        List<ItemRequest> requests = this.itemRequestRepository
                .findAllByRequestorIdOrderByCreatedDesc(userId);
        Map<Long, List<ItemAnswerDto>> answers = this.findAnswers(requests);
        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestWithItemsDto(
                        request, answers.getOrDefault(request.getId(), List.of())))
                .toList();
    }

    @Override
    public List<ItemRequestDto> getAll(Long userId) {
        this.findUserOrThrow(userId);
        return this.itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId).stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .toList();
    }

    @Override
    public ItemRequestWithItemsDto getById(Long userId, Long requestId) {
        this.findUserOrThrow(userId);
        ItemRequest request = this.itemRequestRepository.findById(requestId)
                .orElseThrow(() -> NotFoundException.itemRequest(requestId));
        List<ItemAnswerDto> answers = this.itemRepository.findAllByRequestId(requestId).stream()
                .map(ItemMapper::toItemAnswerDto)
                .toList();
        return ItemRequestMapper.toItemRequestWithItemsDto(request, answers);
    }

    private Map<Long, List<ItemAnswerDto>> findAnswers(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = requests.stream().map(ItemRequest::getId).toList();
        return this.itemRepository.findAllByRequestIdIn(ids).stream()
                .collect(Collectors.groupingBy(Item::getRequestId,
                        Collectors.mapping(ItemMapper::toItemAnswerDto, Collectors.toList())));
    }

    private User findUserOrThrow(Long userId) {
        return this.userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.user(userId));
    }
}
