package ru.uncledrema.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;


@Schema(description = "Полная информация о пользователе")
public record UserInfoDto(
        @Schema(description = "Информация о билетах поезда")
        List<TicketDto> tickets,
        @Schema(description = "Информация о привилегии пользователя")
        PrivilegeShortInfoDto privilege
) {
}
