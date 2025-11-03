package ru.uncledrema.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Операция над ДТО")
public record BalanceOperationDto(
        @Schema(description = "Идентификатор билета")
        UUID ticketUid,
        @Schema(description = "Количество денег для операции")
        int amount
) {
}
