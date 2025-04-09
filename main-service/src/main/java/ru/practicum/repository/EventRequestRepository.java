package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.entity.Request;

public interface EventRequestRepository extends JpaRepository<Request, Long>, QuerydslPredicateExecutor<Request> {

    Request findRequestsByRequester_IdAndEvent_Id(Long requesterId, Long eventId);
}