package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import ru.practicum.entity.Hit;

@Repository
public interface StatRepository extends JpaRepository<Hit, Long>, QuerydslPredicateExecutor<Hit> {
}