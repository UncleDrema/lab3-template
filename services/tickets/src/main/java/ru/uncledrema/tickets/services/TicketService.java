package ru.uncledrema.tickets.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.uncledrema.tickets.dto.BoughtTicketDto;
import ru.uncledrema.tickets.types.Ticket;
import ru.uncledrema.tickets.types.TicketStatus;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class TicketService {
    private final TicketRepository ticketRepository;
    private final FlightClient flightClient;
    private final PrivilegeClient privilegeClient;

    public List<Ticket> findAllByUsername(String username) {
        return ticketRepository.findAllByUsername(username);
    }

    public Ticket findByTicketUid(String username, UUID ticketUid) {
        return ticketRepository.findByUsernameAndTicketUid(username, ticketUid).orElseThrow();
    }

    public void cancelTicket(String username, UUID ticketUid) {
        var ticket = findByTicketUid(username, ticketUid);
        if (ticket.getStatus() != TicketStatus.PAID) {
            throw new IllegalStateException("Ticket is not in PAID status");
        }

        var privilegeAfterCancel = privilegeClient.cancel(username, ticketUid).orElseThrow();
        log.info("Canceling ticket {}, after cancel: {}", ticket.getTicketUid(), privilegeAfterCancel);
        ticket.setStatus(TicketStatus.CANCELED);
        ticketRepository.save(ticket);
    }

    public BoughtTicketDto buyTicket(String flightNumber, int price, boolean paidFromBalance, String username) {
        var flight = flightClient.getFlight(flightNumber).orElseThrow();
        log.info("Flight {} found: {}", flightNumber, flight);
        var userPrivilege = privilegeClient.getPrivilegeForUser(username).orElseThrow();
        log.info("User {} privilege: {}", username, userPrivilege);
        var ticketUid = UUID.randomUUID();
        int moneyPaid;
        int bonusPaid;
        if (paidFromBalance) {
            bonusPaid = Math.min(userPrivilege.balance(), price);
            moneyPaid = price - bonusPaid;
            privilegeClient.withdrawBonuses(username, ticketUid, bonusPaid);
        }
        else {
            moneyPaid = price;
            bonusPaid = 0;
            var bonusEarned = price * 10 / 100;
            privilegeClient.depositBonuses(username, ticketUid, bonusEarned);
        }

        var finalPrivilege = privilegeClient.getPrivilegeForUser(username).orElseThrow();
        var ticket = new Ticket(ticketUid, username, flightNumber, price, TicketStatus.PAID);
        ticketRepository.save(ticket);
        return new BoughtTicketDto(
                ticket.getTicketUid(),
                ticket.getFlightNumber(),
                flight.fromAirport(),
                flight.toAirport(),
                flight.date(),
                ticket.getPrice(),
                moneyPaid,
                bonusPaid,
                ticket.getStatus(),
                finalPrivilege
        );
    }
}
