package ru.practicum.ewm.server.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.server.event.model.Event;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    long countByCategoryId(Long categoryId);

    Page<Event> findByInitiatorId(Long initiatorId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long id, Long initiatorId);

    @Query(value = """
            SELECT *
            FROM events e
            WHERE e.state = 'PUBLISHED'
              AND distance_m(e.location_lat, e.location_lon, :centerLat, :centerLon) <= :radiusM
            """,
            countQuery = """
                    SELECT count(*)
                    FROM events e
                    WHERE e.state = 'PUBLISHED'
                      AND distance_m(e.location_lat, e.location_lon, :centerLat, :centerLon) <= :radiusM
                    """,
            nativeQuery = true)
    Page<Event> findPublishedInRadius(@Param("centerLat") double centerLat,
                                      @Param("centerLon") double centerLon,
                                      @Param("radiusM") int radiusM,
                                      Pageable pageable);
}
