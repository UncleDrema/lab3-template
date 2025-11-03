package ru.uncledrema.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.uncledrema.gateway.types.PrivilegeStatus;

import java.util.List;

@Schema(description = "Информация о привилегии")
public record PrivilegeInfoDto(
        @Schema(description = "Баланс бонусов")
        int balance,
        @Schema(description = "Статус привилегии")
        PrivilegeStatus status,
        @Schema(description = "История изменений баланса")
        List<PrivilegeHistoryItemDto> history
) {
}
