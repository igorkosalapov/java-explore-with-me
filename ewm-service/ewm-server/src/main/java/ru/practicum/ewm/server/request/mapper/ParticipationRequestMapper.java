package ru.practicum.ewm.server.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.server.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.server.request.model.ParticipationRequest;
import ru.practicum.ewm.server.util.DateTimeUtil;

@UtilityClass
public class ParticipationRequestMapper {

    public static ParticipationRequestDto toDto(ParticipationRequest request) {
        if (request == null) {
            return null;
        }
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(DateTimeUtil.format(request.getCreated()))
                .event(request.getEvent() != null ? request.getEvent().getId() : null)
                .requester(request.getRequester() != null ? request.getRequester().getId() : null)
                .status(request.getStatus() != null ? request.getStatus().name() : null)
                .build();
    }
}
