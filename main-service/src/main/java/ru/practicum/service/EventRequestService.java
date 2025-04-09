package ru.practicum.service;

import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.ParticipationRequestDto;

import java.util.List;

public interface EventRequestService {

    ParticipationRequestDto addEventRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> findRequestsParticipationInEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest dto);

    List<ParticipationRequestDto> findAllRequestByUserId(Long userId);

    ParticipationRequestDto cancelRequest(Long requestId, Long userId);
}