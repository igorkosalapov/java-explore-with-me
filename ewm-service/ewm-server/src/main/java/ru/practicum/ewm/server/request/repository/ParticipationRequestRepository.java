package ru.practicum.ewm.server.request.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.server.request.model.ParticipationRequest;
import ru.practicum.ewm.server.request.model.RequestStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    Optional<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long requesterId);

    List<ParticipationRequest> findAllByRequesterId(Long requesterId, Sort sort);

    List<ParticipationRequest> findAllByEventId(Long eventId, Sort sort);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    @Query("select r.event.id as eventId, count(r.id) as requestsCount " +
            "from ParticipationRequest r " +
            "where r.event.id in :eventIds and r.status = :status " +
            "group by r.event.id")
    List<EventRequestsCount> countRequestsByEventIdsAndStatus(@Param("eventIds") Collection<Long> eventIds,
                                                              @Param("status") RequestStatus status);
}
