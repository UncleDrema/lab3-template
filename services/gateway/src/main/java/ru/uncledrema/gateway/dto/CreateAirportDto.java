package ru.uncledrema.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Создание аэропорта")
public record CreateAirportDto(
        @Schema(description = "Название аэропорта")
        String name,
        @Schema(description = "Город, в котором находится аэропорт" )
        String city,
        @Schema(description = "Страна, в которой находится аэропорт")
        String country
) {
}
