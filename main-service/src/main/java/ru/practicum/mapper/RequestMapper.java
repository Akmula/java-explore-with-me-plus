package ru.practicum.mapper;

import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.entity.Event;
import ru.practicum.entity.Request;
import ru.practicum.entity.User;

import java.time.LocalDateTime;

import static ru.practicum.dto.enums.EventRequestStatus.PENDING;

public class RequestMapper {

    public static Request toRequest(User user, Event event) {
        Request request = new Request();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(user);
        request.setStatus(PENDING);
        return request;
    }

    public static ParticipationRequestDto toParticipationRequestDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .requester(request.getId())
                .event(request.getEvent().getId())
                .created(request.getCreated())
                .status(request.getStatus())
                .build();
    }
}