package ru.uncledrema.flights.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.uncledrema.flights.types.Flight;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class FlightService {
    private final FlightRepository flightRepository;
    private final AirportRepository airportRepository;

    public Page<Flight> findAll(Pageable pageable) {
        return flightRepository.findAll(pageable);
    }

    public Flight create(String flightNumber, LocalDateTime datetime, long fromAirportId, long toAirportId, int price) {
        var fromAirport = airportRepository.findById(fromAirportId)
                .orElseThrow(() -> new IllegalArgumentException("From airport not found"));
        var toAirport = airportRepository.findById(toAirportId)
                .orElseThrow(() -> new IllegalArgumentException("To airport not found"));
        var flight = new Flight(flightNumber, datetime.atOffset(ZoneOffset.ofHours(3)), fromAirport, toAirport, price);
        return flightRepository.save(flight);
    }

    public Optional<Flight> findByFlightNumber(String flightNumber) {
        return flightRepository.findByFlightNumber(flightNumber);
    }
}
