package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.entity.Compilation;

public interface CompilationRepository extends JpaRepository<Compilation, Long>,
        QuerydslPredicateExecutor<Compilation> {
}