package ru.uncledrema.tickets.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.uncledrema.tickets.types.PrivilegeStatus;

@Schema(description = "Короткая информация о привилегиях")
public record PrivilegeShortInfoDto(
        @Schema(description = "Баланс привилегии")
        int balance,
        @Schema(description = "Статус привилегии")
        PrivilegeStatus status
) {
}
