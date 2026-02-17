package ru.practicum.ewm.server.location.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.server.location.model.LocationArea;

public interface LocationRepository extends JpaRepository<LocationArea, Long> {
}
