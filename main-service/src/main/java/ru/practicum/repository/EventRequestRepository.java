package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.dto.enums.EventRequestStatus;
import ru.practicum.entity.Request;

import java.util.List;

public interface EventRequestRepository extends JpaRepository<Request, Long>, QuerydslPredicateExecutor<Request> {

    Request findRequestsByRequester_IdAndEvent_Id(Long requesterId, Long eventId);

    long countDistinctByEvent_IdAndStatusEquals(Long eventId, EventRequestStatus status);

    List<Request> findAllByEventIdInAndStatus(List<Long> ids, EventRequestStatus status);
}