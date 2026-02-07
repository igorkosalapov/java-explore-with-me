package ru.practicum.ewm.server.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO заявки на участие в событии.
 *
 * Соответствует схеме ParticipationRequestDto из спецификации основного сервиса.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {

    private String created;

    private Long event;

    private Long id;

    private Long requester;

    private String status;
}
