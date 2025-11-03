package ru.uncledrema.flights.services;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.uncledrema.flights.types.Airport;

@Repository
public interface AirportRepository extends JpaRepository<Airport, Long> {
}
