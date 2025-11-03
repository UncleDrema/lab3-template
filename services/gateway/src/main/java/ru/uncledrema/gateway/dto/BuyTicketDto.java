package ru.uncledrema.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Покупка")
public record BuyTicketDto(
        @Schema(description = "Номер рейса")
        String flightNumber,
        @Schema(description = "Цена билета")
        int price,
        @Schema(description = "Оплачено с баланса")
        boolean paidFromBalance
) {
}
