package ru.practicum.ewm.server.event.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Location {

    @Column(nullable = false)
    private Float lat;

    @Column(nullable = false)
    private Float lon;
}
