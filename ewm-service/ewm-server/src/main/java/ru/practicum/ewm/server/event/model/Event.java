package ru.practicum.ewm.server.event.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewm.server.category.model.Category;
import ru.practicum.ewm.server.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String annotation;

    @Column(nullable = false, length = 7000)
    private String description;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false)
    private Boolean paid = false;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    private LocalDateTime publishedOn;

    @Column(nullable = false)
    private Integer participantLimit = 0;

    @Column(nullable = false)
    private Boolean requestModeration = true;

    @Embedded
    private Location location;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(optional = false)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private State state = State.PENDING;
}