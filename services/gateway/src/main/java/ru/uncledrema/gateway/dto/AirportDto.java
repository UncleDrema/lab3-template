package ru.uncledrema.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Аэропорт")
public record AirportDto(
        @Schema(description = "Идентификатор аэропорта")
        Long id,
        @Schema(description = "Название аэропорта")
        String name,
        @Schema(description = "Город, в котором находится аэропорт" )
        String city,
        @Schema(description = "Страна, в которой находится аэропорт")
        String country
) {
}
