package ru.uncledrema.tickets.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.uncledrema.tickets.dto.BoughtTicketDto;
import ru.uncledrema.tickets.dto.BuyTicketDto;
import ru.uncledrema.tickets.dto.TicketDto;
import ru.uncledrema.tickets.services.TicketService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/tickets")
public class TicketsController {
    private final TicketService ticketService;
    private final Mapper mapper;

    @GetMapping
    public ResponseEntity<List<TicketDto>> getAllForUser(@RequestHeader(value = "X-User-Name") String username) {
        var tickets = ticketService.findAllByUsername(username);
        return ResponseEntity.ok(tickets.stream().map(mapper::toDto).toList());
    }

    @PostMapping
    public ResponseEntity<BoughtTicketDto> buyTicket(@RequestHeader(value = "X-User-Name") String username,
                                                     @RequestBody BuyTicketDto buyTicketDto) {
        var boughtTicket = ticketService.buyTicket(buyTicketDto.flightNumber(), buyTicketDto.price(), buyTicketDto.paidFromBalance(), username);
        return ResponseEntity.ok(boughtTicket);
    }

    @GetMapping("/{ticketUid}")
    public ResponseEntity<TicketDto> getByUid(@RequestHeader(value = "X-User-Name") String username,
                                             @PathVariable UUID ticketUid) {
        var ticket = ticketService.findByTicketUid(username, ticketUid);
        return ResponseEntity.ok(mapper.toDto(ticket));
    }

    @DeleteMapping("/{ticketUid}")
    public ResponseEntity<Void> cancelByUid(@RequestHeader(value = "X-User-Name") String username,
                                            @PathVariable UUID ticketUid) {
        ticketService.cancelTicket(username, ticketUid);
        return ResponseEntity.noContent().build();
    }
}
