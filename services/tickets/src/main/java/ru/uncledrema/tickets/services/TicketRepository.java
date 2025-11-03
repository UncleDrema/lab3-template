package ru.uncledrema.tickets.services;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.uncledrema.tickets.types.Ticket;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findAllByUsername(String username);

    Ticket findByTicketUid(UUID ticketUid);

    Optional<Ticket> findByUsernameAndTicketUid(String username, UUID ticketUid);
}
