package ru.practicum.stats.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.stats.server.model.EndpointHitEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitRepository extends JpaRepository<EndpointHitEntity, Long> {

    @Query("select h.app as app, h.uri as uri, count(h.id) as hits " +
            "from EndpointHitEntity h " +
            "where h.hitTime between :start and :end " +
            "group by h.app, h.uri " +
            "order by hits desc")
    List<ViewStatsProjection> findStats(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("select h.app as app, h.uri as uri, count(distinct h.ip) as hits " +
            "from EndpointHitEntity h " +
            "where h.hitTime between :start and :end " +
            "group by h.app, h.uri " +
            "order by hits desc")
    List<ViewStatsProjection> findUniqueStats(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("select h.app as app, h.uri as uri, count(h.id) as hits " +
            "from EndpointHitEntity h " +
            "where h.hitTime between :start and :end " +
            "and h.uri in (:uris) " +
            "group by h.app, h.uri " +
            "order by hits desc")
    List<ViewStatsProjection> findStatsByUris(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris
    );

    @Query("select h.app as app, h.uri as uri, count(distinct h.ip) as hits " +
            "from EndpointHitEntity h " +
            "where h.hitTime between :start and :end " +
            "and h.uri in (:uris) " +
            "group by h.app, h.uri " +
            "order by hits desc")
    List<ViewStatsProjection> findUniqueStatsByUris(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris
    );
}
