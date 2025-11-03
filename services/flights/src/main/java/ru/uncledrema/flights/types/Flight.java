package ru.uncledrema.flights.types;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "flight")
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(name = "flight_number", nullable = false, length = 20)
    private String flightNumber;

    @NonNull
    @Column(name = "datetime", nullable = false)
    private OffsetDateTime datetime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_airport_id")
    private Airport fromAirport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_airport_id")
    private Airport toAirport;

    @NonNull
    @Column(name = "price", nullable = false)
    private Integer price;

    public Flight(
            @NonNull String flightNumber,
            @NonNull OffsetDateTime datetime,
            Airport fromAirport,
            Airport toAirport,
            @NonNull Integer price) {
        this.flightNumber = flightNumber;
        this.datetime = datetime;
        this.fromAirport = fromAirport;
        this.toAirport = toAirport;
        this.price = price;
    }
}
