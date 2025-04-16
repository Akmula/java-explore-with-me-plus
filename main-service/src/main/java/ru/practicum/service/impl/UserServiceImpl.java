package ru.practicum.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.params.AdminUserParams;
import ru.practicum.entity.User;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    @Transactional
    public UserDto save(NewUserRequest newUserRequest) {
        log.info("UserService - добавление пользователя: {}", newUserRequest);
        User user = repository.save(UserMapper.toUser(newUserRequest));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> findAll(AdminUserParams params) {
        log.info("UserService - получение пользователей с параметрами: {}", params);

        List<Long> ids = params.getIds();
        Integer from = params.getFrom();
        Integer size = params.getSize();

        Pageable pageable = PageRequest.of(from / size, size);

        List<User> users;

        if (ids != null) {
            users = repository.findAllByIdIn(ids, pageable);
        } else {
            users = repository.findAll(pageable).getContent();
        }

        return users
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(Long userId) {
        log.info("UserService - удаление пользователя с id: {}", userId);
        repository.findById(userId).orElseThrow(() -> new NotFoundException(userId));
        repository.deleteById(userId);
    }
}