package ru.uncledrema.tickets.types;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

@Entity
@Table(name = "ticket")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(name = "ticket_uid", nullable = false, unique = true)
    private UUID ticketUid;

    @NonNull
    @Column(name = "username", nullable = false, length = 80)
    private String username;

    @NonNull
    @Column(name = "flight_number", nullable = false, length = 20)
    private String flightNumber;

    @NonNull
    @Column(name = "price", nullable = false)
    private Integer price;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TicketStatus status;

    public Ticket(
            @NonNull UUID ticketUid,
            @NonNull String username,
            @NonNull String flightNumber,
            @NonNull Integer price,
            @NonNull TicketStatus status) {
        this.ticketUid = ticketUid;
        this.username = username;
        this.flightNumber = flightNumber;
        this.price = price;
        this.status = status;
    }
}