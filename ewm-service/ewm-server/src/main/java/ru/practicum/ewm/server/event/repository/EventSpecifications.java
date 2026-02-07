package ru.practicum.ewm.server.event.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.server.event.model.Event;
import ru.practicum.ewm.server.request.model.ParticipationRequest;
import ru.practicum.ewm.server.request.model.RequestStatus;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import java.time.LocalDateTime;
import java.util.List;

public final class EventSpecifications {

    private EventSpecifications() {
    }

    public static Specification<Event> isPublished() {
        return (root, query, cb) -> cb.equal(root.get("state"), Event.State.PUBLISHED);
    }

    public static Specification<Event> fetchCategoryAndInitiator() {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class) {
                root.fetch("category");
                root.fetch("initiator");
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }

    public static Specification<Event> textContainsIgnoreCase(String text) {
        if (text == null || text.isBlank()) {
            return Specification.where(null);
        }
        String pattern = "%" + text.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("annotation")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
        );
    }

    public static Specification<Event> categoryIn(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Specification.where(null);
        }
        return (root, query, cb) -> root.get("category").get("id").in(categoryIds);
    }

    public static Specification<Event> paid(Boolean paid) {
        if (paid == null) {
            return Specification.where(null);
        }
        return (root, query, cb) -> cb.equal(root.get("paid"), paid);
    }

    public static Specification<Event> eventDateAfter(LocalDateTime start) {
        if (start == null) {
            return Specification.where(null);
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("eventDate"), start);
    }

    public static Specification<Event> eventDateBefore(LocalDateTime end) {
        if (end == null) {
            return Specification.where(null);
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("eventDate"), end);
    }

    public static Specification<Event> initiatorIn(List<Long> initiatorIds) {
        if (initiatorIds == null || initiatorIds.isEmpty()) {
            return Specification.where(null);
        }
        return (root, query, cb) -> root.get("initiator").get("id").in(initiatorIds);
    }

    public static Specification<Event> stateIn(List<Event.State> states) {
        if (states == null || states.isEmpty()) {
            return Specification.where(null);
        }
        return (root, query, cb) -> root.get("state").in(states);
    }

    public static Specification<Event> onlyAvailable() {
        return (root, query, cb) -> {
            Subquery<Long> sub = query.subquery(Long.class);
            Root<ParticipationRequest> requestRoot = sub.from(ParticipationRequest.class);
            sub.select(cb.count(requestRoot));
            sub.where(
                    cb.and(
                            cb.equal(requestRoot.get("event"), root),
                            cb.equal(requestRoot.get("status"), RequestStatus.CONFIRMED)
                    )
            );

            Expression<Long> limitAsLong = root.get("participantLimit").as(Long.class);

            Predicate noLimit = cb.equal(root.get("participantLimit"), 0);
            Predicate hasPlaces = cb.lessThan(sub, limitAsLong);
            return cb.or(noLimit, hasPlaces);
        };
    }
}
