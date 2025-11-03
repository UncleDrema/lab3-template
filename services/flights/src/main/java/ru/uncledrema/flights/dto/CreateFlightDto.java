package ru.uncledrema.flights.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Создание рейса")
public record CreateFlightDto(
        @Schema(description = "Номер рейса", example = "AFL031")
        String flightNumber,
        @Schema(
                description = "Дата и время вылета (OffsetDateTime, ISO-8601)",
                example = "2021-10-08 17:00"
        )
        @JsonFormat(
                shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd HH:mm"  // ISO-8601 с временной зоной (+03:00)
        )
        LocalDateTime datetime,
        @Schema(description = "ID Аэропорта вылета", example = "1")
        long fromAirportId,
        @Schema(description = "ID Аэропорта прибытия", example = "2")
        long toAirportId,
        @Schema(description = "Стоимость билета", example = "1500")
        int price
) {
}
