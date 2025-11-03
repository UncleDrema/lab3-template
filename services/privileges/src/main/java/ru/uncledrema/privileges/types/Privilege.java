package ru.uncledrema.privileges.types;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.NonNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.ALL;

@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "privilege")
public class Privilege {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 80, unique = true)
    private String username;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 80)
    private PrivilegeStatus status;

    @Column(name = "balance")
    private int balance;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "privilege", orphanRemoval = true, cascade = ALL)
    private List<PrivilegeHistory> history = new ArrayList<>();

    /**
     * Полный конструктор (явно указываете статус).
     */
    public Privilege(
            @NonNull String username,
            @NonNull PrivilegeStatus status,
            @NonNull Integer balance) {
        this.username = username;
        this.status = status;
        this.balance = balance;
    }

    /**
     * Удобный конструктор — используем BRONZE по умолчанию (как в DDL).
     */
    public Privilege(@NonNull String username, @NonNull Integer balance) {
        this.username = username;
        this.status = PrivilegeStatus.BRONZE;
        this.balance = balance;
    }

    public void withdraw(UUID ticketUID, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        if (getBalance() < amount) {
            throw new IllegalStateException("Not enough balance");
        } else {
            var historyEntry = new PrivilegeHistory(
                    this,
                    ticketUID,
                    LocalDateTime.now(),
                    amount,
                    OperationType.DEBIT_THE_ACCOUNT
            );
            history.add(historyEntry);
            setBalance(getBalance() - amount);
        }
    }

    public void deposit(UUID ticketUID, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        var historyEntry = new PrivilegeHistory(
                this,
                ticketUID,
                LocalDateTime.now(),
                amount,
                OperationType.FILL_IN_BALANCE
        );
        history.add(historyEntry);
        setBalance(getBalance() + amount);
    }

    public void cancel(UUID ticketUid) {
        var ticketHistoryEntry = history.stream()
                .filter(h -> h.getTicketUid().equals(ticketUid))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No history entry for ticket " + ticketUid));
        switch (ticketHistoryEntry.getOperationType()) {
            case DEBIT_THE_ACCOUNT -> deposit(ticketUid, ticketHistoryEntry.getBalanceDiff());
            case FILL_IN_BALANCE -> withdraw(ticketUid, Math.min(ticketHistoryEntry.getBalanceDiff(), getBalance()));
            default -> throw new IllegalStateException("Unknown operation type: " + ticketHistoryEntry.getOperationType());
        }
    }
}
