package ru.uncledrema.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Страница с элементами")
public record PageDto<T>(
        @Schema(description = "Номер страницы (начинается с 1)", example = "1")
        int page,
        @Schema(description = "Размер страницы", example = "10")
        int pageSize,
        @Schema(description = "Общее количество элементов", example = "100")
        long totalElements,
        @Schema(description = "Элементы в странице")
        List<T> items
) {
}
