package ru.practicum.service;

import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.dto.params.PublicCompilationsParams;

import java.util.List;

public interface CompilationService {

    // public

    List<CompilationDto> findCompilation(PublicCompilationsParams params);

    CompilationDto findCompilationById(Long compId);

    // admin

    CompilationDto addCompilation(NewCompilationDto dto);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest dto);

    void removeCompilation(Long compId);
}