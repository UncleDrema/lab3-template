package ru.uncledrema.tickets.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.uncledrema.tickets.dto.TicketDto;
import ru.uncledrema.tickets.services.FlightClient;
import ru.uncledrema.tickets.types.Ticket;

@Service
@RequiredArgsConstructor
public class Mapper {
    private final FlightClient flightClient;

    public TicketDto toDto(Ticket ticket) {
        var flight = flightClient.getFlight(ticket.getFlightNumber()).orElseThrow();
        return new TicketDto(
                ticket.getTicketUid(),
                ticket.getFlightNumber(),
                flight.fromAirport(),
                flight.toAirport(),
                flight.date(),
                ticket.getPrice(),
                ticket.getStatus()
        );
    }
}
