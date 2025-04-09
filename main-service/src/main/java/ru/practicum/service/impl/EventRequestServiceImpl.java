package ru.practicum.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.dto.enums.EventRequestStatus;
import ru.practicum.entity.Event;
import ru.practicum.entity.QRequest;
import ru.practicum.entity.Request;
import ru.practicum.entity.User;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.EventRequestRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.EventRequestService;

import java.util.ArrayList;
import java.util.List;

import static ru.practicum.dto.enums.EventRequestStatus.*;
import static ru.practicum.dto.enums.EventState.PUBLISHED;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class EventRequestServiceImpl implements EventRequestService {

    private final EventRepository eventRepository;
    private final EventRequestRepository eventRequestRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ParticipationRequestDto addEventRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(userId));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(eventId));
        Request userRequest = eventRequestRepository.findRequestsByRequester_IdAndEvent_Id(userId, eventId);
        if (event.getInitiator().equals(user)) {
            throw new ValidationException("Автор события не может подать заявку на участие в событии с id: " + eventId);
        }

        if (!event.getState().equals(PUBLISHED)) {
            throw new ConflictException("Событие не опубликовано!");
        }
        if (userRequest.getRequester().equals(user)) {
            throw new ConflictException("Заявка на участие уже добавлена");
        }

        Request request = RequestMapper.toRequest(user, event);

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            request.setStatus(CONFIRMED);
        }

        Request savedRequest = eventRequestRepository.save(request);

        return RequestMapper.toParticipationRequestDto(savedRequest);
    }

    @Override
    public List<ParticipationRequestDto> findAllRequestByUserId(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(userId));
        BooleanExpression where = QRequest.request.requester.id.eq(userId);
        Iterable<Request> requests = eventRequestRepository.findAll(where);
        List<ParticipationRequestDto> participationRequestDtos = new ArrayList<>();
        for (Request request : requests) {
            participationRequestDtos.add(RequestMapper.toParticipationRequestDto(request));
        }
        return participationRequestDtos;
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long requestId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(userId));
        Request request = eventRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException(requestId));
        if (!request.getRequester().equals(user)) {
            throw new ValidationException("Пользователь с id: " + userId + "," +
                                          " не подавал заявку на участие в событии с id: " + requestId);
        }
        request.setStatus(CANCELED);
        return RequestMapper.toParticipationRequestDto(eventRequestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> findRequestsParticipationInEvent(Long userId, Long eventId) {
        log.info("EventRequestService - получение информации о запросах на участие в событии с id: {}!", eventId);
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(userId));
        BooleanExpression where = QRequest.request.event.initiator.id.eq(userId);
        Iterable<Request> requests = eventRequestRepository.findAll(where);
        List<ParticipationRequestDto> participationRequestDtos = new ArrayList<>();
        for (Request request : requests) {
            participationRequestDtos.add(RequestMapper.toParticipationRequestDto(request));
        }
        return participationRequestDtos;
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest dto) {
        log.info("EventRequestService - изменение статуса заявок на участие в событии с id: {}", eventId);
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(userId));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(eventId));

        if (!event.getInitiator().equals(user)) {
            throw new ValidationException("Пользователь с id:" + userId +
                                          " не является автором события с id: " + eventId);
        }

        List<Long> requestIds = dto.getRequestIds();
        EventRequestStatus status = dto.getStatus();
        final int requestCount = requestIds.size();
        final int limit = event.getParticipantLimit();

        BooleanExpression where = QRequest.request.event.id.eq(eventId);

        Iterable<Request> requests = eventRequestRepository.findAll(where);
        long currentConfirmed = 0;
        for (Request request : requests) {
            if (request.getStatus().equals(CONFIRMED)) {
                currentConfirmed++;
            }
        }
        if (currentConfirmed == limit) {
            throw new ValidationException("Достигнут лимит запросов на участие для этого события: " + eventId);
        }

        for (Request request : requests) {
            for (Long requestId : requestIds) {
                if (requestId.equals(request.getId()) && !request.getStatus().equals(PENDING)) {
                    throw new ConflictException("Статус может быть изменен только для запросов, которые находятся в состоянии ожидания: " + request.getStatus());
                }
            }
        }

        List<Request> updatedRequests = new ArrayList<>();
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        switch (status) {
            case CONFIRMED: {
                if (limit == 0 || !event.getRequestModeration() || currentConfirmed + requestCount <= limit) {
                    for (Request request : requests) {
                        request.setStatus(CONFIRMED);
                        updatedRequests.add(request);
                        confirmedRequests.add(RequestMapper.toParticipationRequestDto(request));
                    }
                } else if (currentConfirmed >= limit) {
                    throw new ConflictException("The request limit for this event has been reached: " + event);
                } else {
                    for (Request request : requests) {
                        if (limit > currentConfirmed) {
                            request.setStatus(CONFIRMED);
                            updatedRequests.add(request);
                            confirmedRequests.add(RequestMapper.toParticipationRequestDto(request));
                            currentConfirmed = currentConfirmed + 1;
                        } else {
                            request.setStatus(REJECTED);
                            updatedRequests.add(request);
                            rejectedRequests.add(RequestMapper.toParticipationRequestDto(request));
                        }
                    }
                }
                break;
            }
            case REJECTED: {
                for (Request request : requests) {
                    request.setStatus(REJECTED);
                    updatedRequests.add(request);
                    rejectedRequests.add(RequestMapper.toParticipationRequestDto(request));
                }
            }
            break;
        }

        eventRequestRepository.saveAll(updatedRequests);
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmedRequests);
        result.setRejectedRequests(rejectedRequests);

        return result;

    }
}