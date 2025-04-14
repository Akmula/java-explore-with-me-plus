package ru.practicum.mapper;

import com.querydsl.core.Tuple;
import org.springframework.stereotype.Component;
import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;
import ru.practicum.entity.Hit;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
public class EndpointHitMapper {

    public Hit endpointToHit(EndpointHit endpointHit) {
        Hit hit = new Hit();
        hit.setApp(endpointHit.getApp());
        hit.setIp(endpointHit.getIp());
        hit.setUri(endpointHit.getUri());
        hit.setTimestamp(LocalDateTime.now());
        return hit;
    }

    public ViewStats toViewStats(Tuple tuple) {
        ViewStats viewStats = new ViewStats();
        viewStats.setApp(tuple.get(0, String.class));
        viewStats.setUri(tuple.get(1, String.class));
        viewStats.setHits(Objects.requireNonNull(tuple.get(2, Long.class)));
        return viewStats;
    }
}