package ru.uncledrema.flights.web;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.uncledrema.flights.dto.CreateFlightDto;
import ru.uncledrema.flights.dto.FlightDto;
import ru.uncledrema.flights.dto.PageDto;
import ru.uncledrema.flights.services.FlightService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/flights")
public class FlightsController {
    private final FlightService flightService;

    @GetMapping
    public ResponseEntity<PageDto<FlightDto>> getAll(
            @RequestParam(name = "page", required = false, defaultValue = "1")
            int page,
            @RequestParam(name = "size", required = false, defaultValue = "20")
            int size
    ) {
        if (page < 0 || size < 1) {
            return ResponseEntity.badRequest().build();
        }
        var flights = flightService.findAll(PageRequest.of(page - 1, size));
        var dtos = flights.stream().map(
                flight -> new FlightDto(flight.getFlightNumber(),
                        flight.getFromAirport().getFullName(),
                        flight.getToAirport().getFullName(),
                        flight.getDatetime().toLocalDateTime(),
                        flight.getPrice())
        ).toList();
        return ResponseEntity.ok(new PageDto<>(
                flights.getNumber() + 1,
                flights.getSize(),
                flights.getTotalElements(),
                dtos
        ));
    }

    @GetMapping("/{flightNumber}")
    public ResponseEntity<FlightDto> getByFlightNumber(
            @PathVariable String flightNumber
    ) {
        var flightOpt = flightService.findByFlightNumber(flightNumber);
        if (flightOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var flight = flightOpt.get();
        var dto = new FlightDto(
                flight.getFlightNumber(),
                flight.getFromAirport().getFullName(),
                flight.getToAirport().getFullName(),
                flight.getDatetime().toLocalDateTime(),
                flight.getPrice()
        );
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<FlightDto> create(
            @RequestBody CreateFlightDto flightCreateDto
    ) {
        if (flightCreateDto.flightNumber() == null ||
                flightCreateDto.datetime() == null) {
            return ResponseEntity.badRequest().build();
        }
        var flight = flightService.create(
                flightCreateDto.flightNumber(),
                flightCreateDto.datetime(),
                flightCreateDto.fromAirportId(),
                flightCreateDto.toAirportId(),
                flightCreateDto.price()
        );
        var dto = new FlightDto(
                flight.getFlightNumber(),
                flight.getFromAirport().getFullName(),
                flight.getToAirport().getFullName(),
                flight.getDatetime().toLocalDateTime(),
                flight.getPrice()
        );
        return ResponseEntity.ok(dto);
    }
}
