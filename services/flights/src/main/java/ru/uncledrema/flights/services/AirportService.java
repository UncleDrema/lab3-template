package ru.uncledrema.flights.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.uncledrema.flights.types.Airport;

@RequiredArgsConstructor
@Service
public class AirportService {
    private final AirportRepository airportRepository;

    public Page<Airport> findAll(Pageable pageable) {
        return airportRepository.findAll(pageable);
    }

    public Airport create(String name, String city, String country) {
        var airport = new Airport(name, city, country);
        return airportRepository.save(airport);
    }
}
