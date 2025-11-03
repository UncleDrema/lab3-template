package ru.uncledrema.tickets.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.uncledrema.tickets.types.TicketStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Купленный билет")
public record BoughtTicketDto(
        @Schema(description = "Идентификатор билета")
        UUID ticketUid,
        @Schema(description = "Номер рейса", example = "AFL031")
        String flightNumber,
        @Schema(description = "Аэропорт вылета (город + название)", example = "Санкт-Петербург Пулково")
        String fromAirport,
        @Schema(description = "Аэропорт прибытия (город + название)", example = "Москва Шереметьево")
        String toAirport,
        @Schema(
                description = "Дата и время вылета",
                example = "2021-10-08 17:00"
        )
        @JsonFormat(
                shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd HH:mm"
        )
        LocalDateTime date,
        @Schema(description = "Стоимость билета", example = "1500")
        int price,
        @Schema(description = "Оплачено деньгами", example = "500")
        int paidByMoney,
        @Schema(description = "Оплачено бонусами", example = "1000")
        int paidByBonuses,
        @Schema(description = "Статус билета")
        TicketStatus status,
        @Schema(description = "Информация о привилегии")
        PrivilegeShortInfoDto privilege
) {
}
