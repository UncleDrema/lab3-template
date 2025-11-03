package ru.uncledrema.privileges.types;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "privilege_history")
public class PrivilegeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "privilege_id")
    @Nullable
    private Privilege privilege;

    @NonNull
    @Column(name = "ticket_uid", nullable = false)
    private UUID ticketUid;

    @NonNull
    @Column(name = "datetime", nullable = false)
    private LocalDateTime datetime;

    @NonNull
    @Column(name = "balance_diff", nullable = false)
    private Integer balanceDiff;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 20)
    private OperationType operationType;

    public PrivilegeHistory(
            @Nullable Privilege privilege,
            @NonNull UUID ticketUid,
            @NonNull LocalDateTime datetime,
            @NonNull Integer balanceDiff,
            @NonNull OperationType operationType) {
        this.privilege = privilege;
        this.ticketUid = ticketUid;
        this.datetime = datetime;
        this.balanceDiff = balanceDiff;
        this.operationType = operationType;
    }
}
