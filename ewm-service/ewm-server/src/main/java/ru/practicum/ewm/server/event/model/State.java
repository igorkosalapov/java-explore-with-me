package ru.practicum.ewm.server.event.model;

import java.util.List;

public enum State {
    PENDING,
    PUBLISHED,
    CANCELED;

    public static State from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("State is blank");
        }
        return State.valueOf(raw.trim().toUpperCase());
    }

    public static List<State> from(List<String> raw) {
        if (raw == null || raw.isEmpty()) return List.of();
        return raw.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(State::from)
                .toList();
    }
}