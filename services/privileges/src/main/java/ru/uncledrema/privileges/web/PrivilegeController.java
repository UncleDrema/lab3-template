package ru.uncledrema.privileges.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.uncledrema.privileges.dto.BalanceOperationDto;
import ru.uncledrema.privileges.dto.PrivilegeHistoryItemDto;
import ru.uncledrema.privileges.dto.PrivilegeInfoDto;
import ru.uncledrema.privileges.services.PrivilegeService;
import ru.uncledrema.privileges.types.Privilege;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/privilege")
public class PrivilegeController {
    private final PrivilegeService privilegeService;

    @GetMapping
    public ResponseEntity<PrivilegeInfoDto> getPrivilege(@RequestHeader(value = "X-User-Name") String username) {
        Privilege privilege = privilegeService.getPrivilegeByUsername(username);

        var dto = mapToDto(privilege);

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<PrivilegeInfoDto> withdraw(
            @RequestHeader(value = "X-User-Name") String username,
            @RequestBody BalanceOperationDto balanceOperation
    ) {
        if (balanceOperation.amount() <= 0) {
            return ResponseEntity.badRequest().build();
        }

        var privilege = privilegeService.withdraw(username, balanceOperation.ticketUid(), balanceOperation.amount());

        var dto = mapToDto(privilege);

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/deposit")
    public ResponseEntity<PrivilegeInfoDto> deposit(
            @RequestHeader(value = "X-User-Name") String username,
            @RequestBody BalanceOperationDto balanceOperation
    ) {
        if (balanceOperation.amount() <= 0) {
            return ResponseEntity.badRequest().build();
        }

        var privilege = privilegeService.deposit(username, balanceOperation.ticketUid(), balanceOperation.amount());

        var dto = mapToDto(privilege);

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/cancel/{ticketUid}")
    public ResponseEntity<PrivilegeInfoDto> cancel(
            @RequestHeader(value = "X-User-Name") String username,
            @PathVariable UUID ticketUid
    ) {
        var privilege = privilegeService.cancel(username, ticketUid);

        var dto = mapToDto(privilege);

        return ResponseEntity.ok(dto);
    }

    private PrivilegeInfoDto mapToDto(Privilege privilege) {
        return new PrivilegeInfoDto(
                privilege.getBalance(),
                privilege.getStatus(),
                privilege.getHistory().stream().map(
                        entry -> new PrivilegeHistoryItemDto(
                                entry.getDatetime(),
                                entry.getTicketUid(),
                                entry.getBalanceDiff(),
                                entry.getOperationType()
                        )
                ).toList()
        );
    }
}
