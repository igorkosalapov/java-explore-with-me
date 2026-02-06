package ru.practicum.ewm.server.compilation.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.ewm.server.event.model.Event;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "compilations", uniqueConstraints = {
        @UniqueConstraint(name = "uq_compilation_name", columnNames = {"title"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false)
    private boolean pinned;

    @ManyToMany
    @JoinTable(
            name = "compilation_events",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    @Builder.Default
    private Set<Event> events = new LinkedHashSet<>();
}
