package ru.uncledrema.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.uncledrema.gateway.types.OperationType;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Элемент истории изменения привилегий")
public record PrivilegeHistoryItemDto(
        @Schema(description = "Дата и время изменения")
        LocalDateTime date,
        @Schema(description = "Идентификатор билета")
        UUID ticketUid,
        @Schema(description = "Изменение баланса бонусов")
        int balanceDiff,
        @Schema(description = "Тип операции")
        OperationType operationType
) {
}
