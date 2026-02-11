package ru.practicum.ewm.server.event.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest {

    private String annotation;

    private Long category;

    private String description;

    private String eventDate;

    private Location location;

    private Boolean paid;

    @NotNull
    private Integer participantLimit;

    private Boolean requestModeration;

    private StateAction stateAction;

    private String title;

    public enum StateAction {
        PUBLISH_EVENT,
        REJECT_EVENT
    }
}
