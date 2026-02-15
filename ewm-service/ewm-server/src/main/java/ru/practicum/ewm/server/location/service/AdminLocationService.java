package ru.practicum.ewm.server.location.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.server.error.exception.NotFoundException;
import ru.practicum.ewm.server.location.dto.LocationDto;
import ru.practicum.ewm.server.location.dto.NewLocationDto;
import ru.practicum.ewm.server.location.dto.UpdateLocationDto;
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

    @Transactional
    public LocationDto update(long locationId, UpdateLocationDto dto) {
        LocationArea loc = getOrThrow(locationId);

        if (dto.getName() != null) {
            String name = dto.getName().trim();
            if (!name.isEmpty()) {
                loc.setName(name);
            }
        }
        if (dto.getLat() != null) {
            loc.setLat(dto.getLat());
        }
        if (dto.getLon() != null) {
            loc.setLon(dto.getLon());
        }
        if (dto.getRadiusM() != null) {
            loc.setRadiusM(dto.getRadiusM());
        }

        LocationArea saved = locationRepository.save(loc);
        return toDto(saved);
    }

    public List<LocationDto> getAll(int from, int size) {
        OffsetBasedPageRequest page =
                new OffsetBasedPageRequest(from, size);

        return locationRepository.findAll(page)
                .stream()
                .map(AdminLocationService::toDto)
                .toList();
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
