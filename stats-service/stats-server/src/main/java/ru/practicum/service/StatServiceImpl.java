package ru.practicum.service;


import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;
import ru.practicum.entity.QHit;
import ru.practicum.exception.BadRequestException;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.param.RequestParams;
import ru.practicum.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatServiceImpl implements StatService {

    private final EntityManager entityManager;
    private final StatRepository repository;
    private final EndpointHitMapper mapper;

    @Override
    @Transactional
    public void save(EndpointHit hit) {
        log.info("StatService - сохранение hit: {}", hit);
        repository.save(mapper.endpointToHit(hit));
    }

    @Override
    public List<ViewStats> getViewStats(RequestParams requestParams) {
        log.info("StatService - получение статистики с параметрами: {}", requestParams);

        LocalDateTime startTime = requestParams.getStart();
        LocalDateTime endTime = requestParams.getEnd();
        List<String> uris = requestParams.getUris();
        boolean unique = requestParams.isUnique();

        if (startTime == null || endTime == null) {
            throw new BadRequestException("Даты начала и окончания должны быть указаны!");
        }

        if (Objects.requireNonNull(startTime).isAfter(endTime)) {
            throw new BadRequestException("Дата начала не может быть позже даты окончания!");
        }

        QHit qHit = QHit.hit;
        JPAQuery<Tuple> query = new JPAQuery<>(entityManager);

        BooleanExpression where = qHit.timestamp.between(startTime, endTime);

        if (uris != null && !uris.isEmpty()) {
            where = where.and(qHit.uri.in(uris));
        }

        if (unique) {
            query.select(qHit.ip.countDistinct(), qHit.app, qHit.uri)
                    .from(qHit).where(where)
                    .groupBy(qHit.app, qHit.uri)
                    .orderBy(qHit.ip.countDistinct().desc());
        } else {
            query.select(qHit.ip.count(), qHit.app, qHit.uri)
                    .from(qHit)
                    .where(where)
                    .groupBy(qHit.app, qHit.uri)
                    .orderBy(qHit.ip.count().desc());
        }

        List<Tuple> tuples = query.fetch();

        return tuples.stream().map(mapper::toViewStats).collect(Collectors.toList());
    }
}