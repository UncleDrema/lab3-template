package ru.uncledrema.gateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.uncledrema.gateway.types.PrivilegeStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Короткая информация о привилегиях")
public record PrivilegeShortInfoDto(
        @Schema(description = "Баланс привилегии")
        Integer balance,
        @Schema(description = "Статус привилегии")
        PrivilegeStatus status
) {
}
