package ru.practicum.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.dto.params.PublicCompilationsParams;
import ru.practicum.entity.Compilation;
import ru.practicum.entity.Event;
import ru.practicum.entity.QCompilation;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.CompilationService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final EventRepository eventRepository;
    private final CompilationRepository compilationRepository;

    @Override
    public List<CompilationDto> findCompilation(PublicCompilationsParams params) {
        log.info("CompilationService - получение подборок событий!");
        Boolean pinned = params.getPinned();
        int from = params.getFrom();
        int size = params.getSize();

        Pageable pageable = PageRequest.of(from / size, size);

        Page<Compilation> compilations;
        BooleanExpression where;
        if (pinned != null) {
            where = QCompilation.compilation.pinned.eq(pinned);
            compilations = compilationRepository.findAll(where, pageable);
        } else {
            compilations = compilationRepository.findAll(pageable);
        }

        return compilations.getContent()
                .stream()
                .map(CompilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto findCompilationById(Long compId) {
        log.info("CompilationService - получение подборки событий по id: {}", compId);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(compId));
        return CompilationMapper.toCompilationDto(compilation);
    }

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto dto) {
        log.info("CompilationService - добавление подборки: {}", dto);
        List<Long> eventIds = dto.getEvents();
        List<Event> events = eventRepository.findAllById(eventIds);
        Set<Event> eventsSet = new HashSet<>(events);
        Compilation compilation = CompilationMapper.toCompilation(dto, eventsSet);
        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest dto) {
        log.info("CompilationService - обновление подборки: {}", dto);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(compId));

        List<Long> eventIds = dto.getEvents();
        Boolean pinned = dto.getPinned();
        String title = dto.getTitle();

        if (!eventIds.isEmpty()) {
            List<Event> events = eventRepository.findAllById(eventIds);
            Set<Event> eventsSet = new HashSet<>(events);
            compilation.setEvents(eventsSet);
        }

        if (pinned != null) {
            compilation.setPinned(pinned);
        }

        if (title != null) {
            compilation.setTitle(title);
        }

        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    @Transactional
    public void removeCompilation(Long compId) {
        log.info("CompilationService - удаление подборки: {}", compId);
        compilationRepository.findById(compId).orElseThrow(() -> new NotFoundException(compId));
        compilationRepository.deleteById(compId);
    }
}