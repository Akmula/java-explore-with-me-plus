package ru.practicum.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import ru.practicum.entity.*;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.param.RequestParams;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.EventRequestRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.dto.enums.AdminActionState.PUBLISH_EVENT;
import static ru.practicum.dto.enums.AdminActionState.REJECT_EVENT;
import static ru.practicum.dto.enums.EventRequestStatus.CONFIRMED;
import static ru.practicum.dto.enums.EventState.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRequestRepository eventRequestRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final StatsClient statsClient;

    // private

    @Override
    public List<EventShortDto> findAllByUserId(Long userId, PrivateEventParams params) {
        log.info("EventService - Получение событий пользователем с id: {}", userId);

        findUserById(userId);

        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        Page<Event> page = eventRepository.findAllByInitiatorId(userId, pageable);

        List<Event> events = setViewsInEventAndConfirmedRequests(page.getContent());

        return events.stream().map(EventMapper::toEventShortDto).toList();
    }

    @Override
    @Transactional
    public EventFullDto add(Long userId, NewEventDto dto) {
        log.info("EventService - Добавление пользователем с id: {}, события: {}", userId, dto);

        User user = findUserById(userId);
        Category category = findCategoryById(dto.getCategory());

        if (dto.getEventDate() != null && dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Дата и время на которые намечено событие не может быть раньше," +
                                          " чем через два часа от текущего момента.");
        }

        Event event = EventMapper.toEvent(dto);
        event.setInitiator(user);
        event.setCategory(category);

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto findUserEventById(Long userId, Long eventId) {
        log.info("EventService - Получение события с id: {}, пользователем с  id: {}", eventId, userId);

        findUserById(userId);
        BooleanExpression byEventId = QEvent.event.id.eq(eventId);
        Event event = eventRepository.findOne(byEventId).orElseThrow(() -> new NotFoundException(eventId));

        return EventMapper.toEventFullDto(setViewsInEventAndConfirmedRequests(List.of(event)).getFirst());
    }

    @Override
    @Transactional
    public EventFullDto updateUserEventById(Long userId, Long eventId, UpdateEventUserRequest dto) {
        log.info("EventService - Обновление события с id: {}, пользователем с  id: {}", eventId, userId);

        User user = findUserById(userId);
        BooleanExpression byEventId = QEvent.event.id.eq(eventId);
        Event oldEvent = eventRepository.findOne(byEventId).orElseThrow(() -> new NotFoundException(eventId));

        if (!oldEvent.getInitiator().equals(user)) {
            throw new ValidationException("Пользователь с id: %s, не является автором события с id: %s"
                    .formatted(userId, eventId));
        }

        if (dto.getEventDate() != null && dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Дата и время на которые намечено событие не может быть раньше," +
                                          " чем через два часа от текущего момента.");
        }

        if (oldEvent.getState() == PUBLISHED) {
            throw new ConflictException("Изменить можно только отмененные события или" +
                                        " события в состоянии ожидания модерации");
        }

        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case SEND_TO_REVIEW:
                    oldEvent.setState(PENDING);
                    break;
                case CANCEL_REVIEW:
                    oldEvent.setState(CANCELED);
                    break;
                default:
                    throw new BadRequestException("Unexpected value: " + dto.getStateAction());
            }
        }

        Event updatedEvent = updateEventFields(oldEvent, dto);

        Event event = eventRepository.save(updatedEvent);

        return EventMapper.toEventFullDto(setViewsInEventAndConfirmedRequests(List.of(event)).getFirst());
    }

    // public

    @Override
    public List<EventShortDto> searchEvents(PublicEventParams params, HttpServletRequest request) {
        log.info("PublicEventService - Поиск событий с парамерами: {}", params);

        String text = params.getText();
        List<Long> categories = params.getCategories();
        Boolean paid = params.getPaid();
        LocalDateTime rangeStart = params.getRangeStart();
        LocalDateTime rangeEnd = params.getRangeEnd();
        Boolean onlyAvailable = params.getOnlyAvailable();
        String sort = params.getSort();
        int from = params.getFrom();
        int size = params.getSize();

        QEvent qEvent = QEvent.event;

        BooleanExpression where = qEvent.state.eq(PUBLISHED);

        if (categories != null && !categories.isEmpty()) {
            if (categories.size() == 1 && categories.getFirst() == 0L) {
                throw new BadRequestException("Неверно указана категория!");
            }
            where = where.and(qEvent.category.id.in(categories));
        }

        if (text != null) {
            where = where
                    .and(qEvent.annotation.lower().like("%" + text.toLowerCase() + "%")
                            .or(qEvent.description.lower().like("%" + text.toLowerCase() + "%"))
                    );
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

        Pageable pageable = PageRequest.of(from / size, size);

        Page<Event> page = eventRepository.findAll(where, pageable);

        List<Event> events = setViewsInEventAndConfirmedRequests(page.getContent());

        if (onlyAvailable) {
            events.removeIf(event -> event.getParticipantLimit() < event.getConfirmedRequests());
        }

        List<EventShortDto> eventShorts = events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());

        saveHit(request);

        if (sort == null) {
            return eventShorts;
        }

        return switch (sort) {
            case "VIEWS" -> eventShorts
                    .stream()
                    .sorted(Comparator.comparingLong(EventShortDto::getViews).reversed())
                    .collect(Collectors.toList());
            case "EVENT_DATE" -> eventShorts
                    .stream()
                    .sorted(Comparator.comparing(EventShortDto::getEventDate))
                    .collect(Collectors.toList());
            default -> eventShorts;
        };
    }

    @Override
    public EventFullDto findEventById(Long eventId, HttpServletRequest request) {
        log.info("PublicEventService - Получение события по id: {}", eventId);

        BooleanExpression where = QEvent.event.id.eq(eventId).and(QEvent.event.state.eq(PUBLISHED));
        Event event = eventRepository.findOne(where).orElseThrow(() -> new NotFoundException(eventId));
        List<Event> events = setViewsInEventAndConfirmedRequests(List.of(event));

        saveHit(request);

        return EventMapper.toEventFullDto(events.getFirst());
    }

    // admin

    @Override
    public List<EventFullDto> search(AdminEventParams params) {
        log.info("AdminEventService - Поиск событий админом с параметрами: {}", params);
        List<Long> users = params.getUsers();
        List<Long> categories = params.getCategories();
        List<EventState> states = params.getStates();
        LocalDateTime rangeStart = params.getRangeStart();
        LocalDateTime rangeEnd = params.getRangeEnd();
        int from = params.getFrom();
        int size = params.getSize();

        Pageable pageable = PageRequest.of(from / size, size);

        QEvent qEvent = QEvent.event;
        BooleanBuilder where = new BooleanBuilder();

        if (states != null && !states.isEmpty()) {
            where.and(qEvent.state.in(states));
            System.out.println(states);
        }

        if (users != null && !users.isEmpty()) {
            if (users.size() == 1 && users.getFirst() == 0L) {
                throw new BadRequestException("");
            }
            where.and(qEvent.initiator.id.in(users));
        }

        if (categories != null && !categories.isEmpty()) {
            if (categories.size() == 1 && categories.getFirst() == 0L) {
                throw new BadRequestException("");
            }
            where.and(qEvent.category.id.in(categories));
        }

        if (rangeStart != null) {
            where.and(qEvent.eventDate.after(rangeStart));
        }

        if (rangeEnd != null) {
            where.and(qEvent.eventDate.before(rangeEnd));
        }

        Page<Event> page = eventRepository.findAll(where, pageable);

        List<EventFullDto> eventsFullDto = new ArrayList<>();

        if (!page.isEmpty()) {
            List<Event> events = setViewsInEventAndConfirmedRequests(page.getContent());
            eventsFullDto = events.stream()
                    .map(EventMapper::toEventFullDto)
                    .collect(Collectors.toList());
        }
        return eventsFullDto;
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest dto) {
        log.info("AdminEventService - Обновление события админом: {}", dto);

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(eventId));

        if (dto.getEventDate() != null && dto.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new BadRequestException("Дата начала события должна быть не ранее чем за час от даты публикации.");
        }

        if (dto.getStateAction() != null && dto.getStateAction().equals(PUBLISH_EVENT) && event.getState().equals(PUBLISHED)) {
            throw new ConflictException("Событие уже опубликовано!");
        }

        if (dto.getStateAction() != null && dto.getStateAction().equals(REJECT_EVENT) && event.getState().equals(PUBLISHED)) {
            throw new ConflictException("Нельзя отменить опубликованное событие!");
        }

        if (dto.getStateAction() != null && dto.getStateAction().equals(PUBLISH_EVENT) && event.getState().equals(CANCELED)) {
            throw new ConflictException("Нельзя опубликовать отмененное событие!");
        }

        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case REJECT_EVENT -> event.setState(CANCELED);
                case PUBLISH_EVENT -> {
                    event.setState(PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
            }
        }

        Event updatedEvent = updateEventFields(event, dto);
        Event savedEvent = eventRepository.save(updatedEvent);

        return EventMapper.toEventFullDto(setViewsInEventAndConfirmedRequests(List.of(savedEvent)).getFirst());
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException(userId));
    }

    private Category findCategoryById(Long catId) {
        return categoryRepository.findById(catId).orElseThrow(() -> new NotFoundException(catId));
    }

    private Event updateEventFields(Event event, UpdateEventRequest request) {
        log.info("Обновление полей события: {}", request);

        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            event.setCategory(findCategoryById(request.getCategory()));
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }
        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }

        return event;
    }

    private List<Event> setViewsInEventAndConfirmedRequests(List<Event> events) {
        log.info("Установка количества просмотров и подтвержденных запросов в событии.");

        List<Long> eventIds = events.stream().map(Event::getId).toList();

        List<String> uris = events
                .stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        LocalDateTime start = Objects.requireNonNull(events
                .stream()
                .map(Event::getCreatedOn)
                .toList()
                .stream()
                .min(LocalDateTime::compareTo)
                .orElse(null));

        LocalDateTime end = LocalDateTime.now();

        RequestParams params = new RequestParams();
        params.setStart(start);
        params.setEnd(end);
        params.setUris(uris);
        params.setUnique(true);

        List<ViewStats> views = statsClient.getStats(params);
        List<Request> requests = eventRequestRepository
                .findAllByEventIdInAndStatus(eventIds, CONFIRMED);

        Map<Long, Long> mapViews = new HashMap<>();

        if (!views.isEmpty()) {
            views.forEach(v -> mapViews.put(Long.parseLong(v.getUri().split("/", 0)[2]),
                    v.getHits()));
        }

        events.forEach(event -> event.setViews(mapViews.getOrDefault(event.getId(), 0L)));

        Map<Long, Long> mapRequests = eventIds
                .stream()
                .collect(Collectors
                        .toMap(eventId -> eventId,
                                eventId -> (long) requests
                                        .stream()
                                        .collect(Collectors
                                                .groupingBy(request -> request.getEvent().getId()))
                                        .getOrDefault(eventId,
                                                List.of()).size(), (a, b) -> b));

        events.forEach(event -> event
                .setConfirmedRequests(mapRequests.getOrDefault(event.getId(), 0L)));

        return events;
    }

    private void saveHit(HttpServletRequest request) {
        log.info("Сохранение Hit - {}", request.getRemoteAddr());

        statsClient.saveHit(EndpointHit.builder()
                .app("ewm-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .build());
    }
}