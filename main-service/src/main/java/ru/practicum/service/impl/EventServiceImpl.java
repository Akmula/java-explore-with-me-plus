package ru.practicum.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHit;
import ru.practicum.StatsClient;
import ru.practicum.ViewStats;
import ru.practicum.dto.*;
import ru.practicum.dto.enums.EventState;
import ru.practicum.dto.params.AdminEventParams;
import ru.practicum.dto.params.PrivateEventParams;
import ru.practicum.dto.params.PublicEventParams;
import ru.practicum.entity.Category;
import ru.practicum.entity.Event;
import ru.practicum.entity.QEvent;
import ru.practicum.entity.User;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.param.RequestParams;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.dto.enums.EventState.CANCELED;
import static ru.practicum.dto.enums.EventState.PENDING;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final StatsClient statsClient;

    // private Endpoint

    @Override
    public List<EventShortDto> findAllByUserId(Long userId, PrivateEventParams params) {
        log.info("EventService - Получение событий пользователем с id: {}", userId);
        User user = findUserById(userId);

        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());

        Page<Event> page = eventRepository.findAllByInitiatorId(userId, pageable);

        return page.getContent().stream().map(EventMapper::toEventShortDto).toList();
    }

    @Override
    @Transactional
    public EventFullDto add(Long userId, NewEventDto dto) {
        log.info("EventService - Добавление пользователем с id: {}, события: {}", userId, dto);
        User user = findUserById(userId);
        Category category = findCategoryById(dto.getCategory());

        Event event = EventMapper.toEvent(dto);

        event.setInitiator(user);
        event.setCategory(category);

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto findUserEventById(Long userId, Long eventId) {
        User user = findUserById(userId);

        BooleanExpression byEventId = QEvent.event.id.eq(eventId);
        Event event = eventRepository.findOne(byEventId).orElseThrow(() -> new NotFoundException(eventId));

        return EventMapper.toEventFullDto(setViewsInEvent(List.of(event)).getFirst());
    }

    @Override
    @Transactional
    public EventFullDto updateUserEventById(Long userId, Long eventId, UpdateEventUserRequest dto) {
        User user = findUserById(userId);
        BooleanExpression byEventId = QEvent.event.id.eq(eventId);
        Event oldEvent = eventRepository.findOne(byEventId).orElseThrow(() -> new NotFoundException(eventId));

        if (!oldEvent.getInitiator().equals(user)) {
            throw new ValidationException("Пользователь с id: %s, не является автором события с id: %s"
                    .formatted(userId, eventId));
        }

        if (dto.getAnnotation() != null) {
            oldEvent.setAnnotation(dto.getAnnotation());
        }
        if (dto.getCategory() != null) {
            oldEvent.setCategory(findCategoryById(dto.getCategory().getId()));
        }
        if (dto.getDescription() != null) {
            oldEvent.setDescription(dto.getDescription());
        }
        if (dto.getEventDate() != null) {
            oldEvent.setEventDate(dto.getEventDate());
        }
        if (dto.getLocation() != null) {
            oldEvent.setLocation(dto.getLocation());
        }
        if (dto.getPaid() != null) {
            oldEvent.setPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            oldEvent.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            oldEvent.setRequestModeration(dto.getRequestModeration());
        }
        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case SEND_TO_REVIEW:
                    oldEvent.setState(PENDING);
                case CANCEL_REVIEW:
                    oldEvent.setState(CANCELED);
            }
        }
        if (dto.getTitle() != null) {
            oldEvent.setTitle(dto.getTitle());
        }

        Event event = eventRepository.save(oldEvent);

        return EventMapper.toEventFullDto(setViewsInEvent(List.of(event)).getFirst());
    }

    // public Endpoint

    @Override
    public List<EventShortDto> searchEvents(PublicEventParams params, HttpServletRequest request) {

        log.info("PublicEventService - Поиск событий с парамерами: {}", params);
        final String text = params.getText().toLowerCase();
        final List<Long> categories = params.getCategories();
        final Boolean paid = params.getPaid();
        final LocalDateTime rangeStart = params.getRangeStart();
        final LocalDateTime rangeEnd = params.getRangeEnd();
        final Boolean onlyAvailable = params.getOnlyAvailable();
        final String sort = params.getSort();
        final int from = params.getFrom();
        final int size = params.getSize();

        Sort orders = switch (sort) {
            case "EVENT_DATE" -> Sort.by(Sort.Direction.DESC, "eventDate");
            case "VIEWS" -> Sort.by(Sort.Direction.DESC, "views");
            default -> Sort.by(Sort.Direction.ASC, "id");
        };
        QEvent qEvent = QEvent.event;

        BooleanExpression where = qEvent.state.eq(EventState.PUBLISHED);

        if (categories != null && !categories.isEmpty()) {
            where = where.and(qEvent.category.id.in(categories));
        }

        if (!text.isEmpty()) {
            where = where.and(qEvent.annotation.lower().like("%" + text + "%").or(qEvent.description.lower().like("%" + text + "%")));
        }

        if (paid != null) {
            where = where.and(qEvent.paid.eq(paid));
        }
        if (rangeStart == null && rangeEnd == null) {
            where = where.and(qEvent.eventDate.after(LocalDateTime.now()));
        }
        if (rangeStart != null) {
            where = where.and(qEvent.eventDate.after(rangeStart));
        }
        if (rangeEnd != null) {
            where = where.and(qEvent.eventDate.before(rangeEnd));
        }

        if (!onlyAvailable) {
// тут еще надо это доделать
            where = where.and(qEvent.participantLimit.eq(0));
        }

        Pageable pageable = PageRequest.of(from / size, size, orders);

        Page<Event> page = eventRepository.findAll(where, pageable);

        List<EventShortDto> events1 = new ArrayList<>();
        List<Event> events = setViewsInEvent(page.getContent());
        for (Event event : events) {
            events1.add(EventMapper.toEventShortDto(event));
        }

        saveHit(request);
        return events1;
    }

    @Override
    public EventFullDto findEventById(Long eventId, HttpServletRequest request) {
        log.info("PublicEventService - Получение события по id: {}", eventId);

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(eventId));

        List<Event> events = setViewsInEvent(List.of(event));
        saveHit(request);
        return EventMapper.toEventFullDto(events.getFirst());
    }

    // admin Endpoint

    @Override
    public List<EventFullDto> search(AdminEventParams params) {
        log.info("AdminEventService - Поиск событий админом с параметрами: {}", params);
        final List<Long> users = params.getUsers();
        final List<Long> categories = params.getCategories();
        final List<EventState> states = params.getStates();
        final LocalDateTime rangeStart = params.getRangeStart();
        final LocalDateTime rangeEnd = params.getRangeEnd();
        final int from = params.getFrom();
        final int size = params.getSize();

        Pageable pageable = PageRequest.of(from / size, size);

        QEvent qEvent = QEvent.event;
        BooleanExpression where = qEvent.state.in(states);

        if (rangeStart.isBefore(rangeEnd) && rangeEnd != null) {
            where = qEvent.createdOn.between(rangeStart, rangeEnd);
        }

        if (!users.isEmpty()) {
            where = qEvent.initiator.id.in(users);
        }

        if (!categories.isEmpty()) {
            where = qEvent.category.id.in(categories);
        }

        Page<Event> page = eventRepository.findAll(where, pageable);

        List<EventFullDto> events1 = new ArrayList<>();
        List<Event> events = setViewsInEvent(page.getContent());
        for (Event event : events) {

            events1.add(EventMapper.toEventFullDto(event));
        }

        return events1;
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest dto) {
        log.info("AdminEventService - Обновление события админом: {}", dto);
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(eventId));

        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }

        if (dto.getCategory() != null) {
            event.setCategory(findCategoryById(dto.getCategory()));
        }

        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getEventDate() != null) {
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getLocation() != null) {
            event.setLocation(dto.getLocation());
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }

        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case REJECT_EVENT -> event.setState(EventState.CANCELED);
                case PUBLISH_EVENT -> event.setState(PENDING);
            }
        }
        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException(userId));
    }

    private Category findCategoryById(Long catId) {
        return categoryRepository.findById(catId).orElseThrow(() -> new NotFoundException(catId));
    }

    private List<Event> setViewsInEvent(List<Event> events) {
        log.info("Установка просмотров в событии");
        if (events == null || events.isEmpty()) {
            return events;
        }

        List<String> uris = events
                .stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        LocalDateTime start = events
                .stream()
                .map(Event::getCreatedOn)
                .toList()
                .stream()
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime end = LocalDateTime.now();

        RequestParams params = new RequestParams();
        params.setStart(start);
        params.setEnd(end);
        params.setUris(uris);
        params.setUnique(true);

        List<ViewStats> views = statsClient.getStats(params);

        Map<Long, Long> map = new HashMap<>();
        if (!views.isEmpty()) {
            views.forEach(v -> map.put(Long.parseLong(v.getUri().split("/", 0)[2]),
                    v.getHits()));
        }

        for (Event event : events) {
            event.setViews(map.getOrDefault(event.getId(), 0L));
        }

        return events;
    }

    private void saveHit(HttpServletRequest request) {
        log.info("Сохранение Hit - {}", request.getRemoteAddr());
        statsClient.saveHit(EndpointHit.builder()
                .app("ewm-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
    }
}