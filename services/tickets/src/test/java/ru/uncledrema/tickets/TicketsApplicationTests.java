package ru.uncledrema.tickets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.uncledrema.tickets.dto.BoughtTicketDto;
import ru.uncledrema.tickets.dto.FlightDto;
import ru.uncledrema.tickets.dto.PrivilegeShortInfoDto;
import ru.uncledrema.tickets.services.FlightClient;
import ru.uncledrema.tickets.services.PrivilegeClient;
import ru.uncledrema.tickets.services.TicketRepository;
import ru.uncledrema.tickets.services.TicketService;
import ru.uncledrema.tickets.types.Ticket;
import ru.uncledrema.tickets.types.TicketStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TicketsApplicationTests {

    private TicketRepository ticketRepository;
    private FlightClient flightClient;
    private PrivilegeClient privilegeClient;
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        flightClient = mock(FlightClient.class);
        privilegeClient = mock(PrivilegeClient.class);
        ticketService = new TicketService(ticketRepository, flightClient, privilegeClient);
    }

    @Test
    void findAllByUsername_returnsTickets() {
        String username = "user";
        List<Ticket> tickets = List.of(new Ticket(UUID.randomUUID(), username, "FL123", 100, TicketStatus.PAID));
        when(ticketRepository.findAllByUsername(username)).thenReturn(tickets);

        List<Ticket> result = ticketService.findAllByUsername(username);

        assertEquals(1, result.size());
        assertEquals(username, result.get(0).getUsername());
    }

    @Test
    void findByTicketUid_returnsTicket() {
        String username = "user";
        UUID ticketUid = UUID.randomUUID();
        Ticket ticket = new Ticket(ticketUid, username, "FL123", 100, TicketStatus.PAID);
        when(ticketRepository.findByUsernameAndTicketUid(username, ticketUid)).thenReturn(Optional.of(ticket));

        Ticket result = ticketService.findByTicketUid(username, ticketUid);

        assertEquals(ticketUid, result.getTicketUid());
    }

    @Test
    void cancelTicket_throwsIfNotPaid() {
        String username = "user";
        UUID ticketUid = UUID.randomUUID();
        Ticket ticket = new Ticket(ticketUid, username, "FL123", 100, TicketStatus.CANCELED);
        when(ticketRepository.findByUsernameAndTicketUid(username, ticketUid)).thenReturn(Optional.of(ticket));

        assertThrows(IllegalStateException.class, () -> ticketService.cancelTicket(username, ticketUid));
    }

    @Test
    void buyTicket_paidFromBalance() {
        String username = "user";
        String flightNumber = "FL123";
        int price = 100;
        var flight = mock(FlightDto.class);
        var privilege = mock(PrivilegeShortInfoDto.class);

        when(flightClient.getFlight(flightNumber)).thenReturn(Optional.of(flight));
        when(privilegeClient.getPrivilegeForUser(username)).thenReturn(Optional.of(privilege));
        when(privilege.balance()).thenReturn(50);
        when(privilegeClient.getPrivilegeForUser(username)).thenReturn(Optional.of(privilege));

        BoughtTicketDto dto = ticketService.buyTicket(flightNumber, price, true, username);

        assertEquals(flightNumber, dto.flightNumber());
        assertEquals(TicketStatus.PAID, dto.status());
    }
}
