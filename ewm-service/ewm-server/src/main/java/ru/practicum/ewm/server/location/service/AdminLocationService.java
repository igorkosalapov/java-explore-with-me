package ru.practicum.ewm.server.location.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.server.error.exception.NotFoundException;
import ru.practicum.ewm.server.location.dto.LocationDto;
import ru.practicum.ewm.server.location.dto.NewLocationDto;
import ru.practicum.ewm.server.location.model.LocationArea;
import ru.practicum.ewm.server.location.repository.LocationRepository;
import ru.practicum.ewm.server.util.OffsetBasedPageRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLocationService {

    private final LocationRepository locationRepository;

    @Transactional
    public LocationDto add(NewLocationDto dto) {
        LocationArea loc = new LocationArea();
        loc.setName(dto.getName().trim());
        loc.setLat(dto.getLat());
        loc.setLon(dto.getLon());
        loc.setRadiusM(dto.getRadiusM());

        LocationArea saved = locationRepository.save(loc);
        return toDto(saved);
    }

    public List<LocationDto> getAll(int from, int size) {
        var page = new OffsetBasedPageRequest(from, size, Sort.by("id").ascending());
        return locationRepository.findAll(page).stream().map(AdminLocationService::toDto).toList();
    }

    public LocationArea getOrThrow(long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location with id=" + id + " was not found"));
    }

    private static LocationDto toDto(LocationArea a) {
        return LocationDto.builder()
                .id(a.getId())
                .name(a.getName())
                .lat(a.getLat())
                .lon(a.getLon())
                .radiusM(a.getRadiusM())
                .build();
    }
}
