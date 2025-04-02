package ru.practicum.service;

import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;
import ru.practicum.param.RequestParams;

import java.util.List;

public interface StatService {

    void save(EndpointHit hit);

    List<ViewStats> getViewStats(RequestParams requestParams);
}