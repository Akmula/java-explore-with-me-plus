package ru.practicum.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.*;
import ru.practicum.dto.params.AdminEventParams;
import ru.practicum.dto.params.PrivateEventParams;
import ru.practicum.dto.params.PublicEventParams;

import java.util.List;

public interface EventService {

    // private Endpoint

    List<EventShortDto> findAllByUserId(Long userId, PrivateEventParams params);

    EventFullDto add(Long userId, NewEventDto dto);

    EventFullDto findUserEventById(Long userId, Long eventId);

    EventFullDto updateUserEventById(Long userId, Long eventId, UpdateEventUserRequest dto);

    // public Endpoint

    List<EventShortDto> searchEvents(PublicEventParams params, HttpServletRequest request);

    EventFullDto findEventById(Long eventId, HttpServletRequest request);

    // admin Endpoint

    List<EventFullDto> search(AdminEventParams params);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest dto);

}