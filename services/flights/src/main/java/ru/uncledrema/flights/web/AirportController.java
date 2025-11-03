package ru.uncledrema.flights.web;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.uncledrema.flights.dto.AirportDto;
import ru.uncledrema.flights.dto.CreateAirportDto;
import ru.uncledrema.flights.dto.PageDto;
import ru.uncledrema.flights.services.AirportService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/airports")
public class AirportController {
    private final AirportService airportService;

    @GetMapping
    public ResponseEntity<PageDto<AirportDto>> getAll(
            @RequestParam(name = "page", required = false, defaultValue = "1")
            int page,
            @RequestParam(name = "size", required = false, defaultValue = "20")
            int size
    ) {
        if (page < 0 || size < 1) {
            return ResponseEntity.badRequest().build();
        }
        var airports = airportService.findAll(PageRequest.of(page - 1, size));
        var dtos = airports.stream().map(
                flight -> new AirportDto(flight.getId(),
                        flight.getName(),
                        flight.getCity(),
                        flight.getCountry())
        ).toList();
        return ResponseEntity.ok(new PageDto<>(
                airports.getNumber() + 1,
                airports.getSize(),
                airports.getTotalElements(),
                dtos
        ));
    }

    @PostMapping
    public ResponseEntity<AirportDto> create(@RequestBody CreateAirportDto createAirportDto) {
        if (createAirportDto.name() == null || createAirportDto.city() == null || createAirportDto.country() == null) {
            return ResponseEntity.badRequest().build();
        }
        var airport = airportService.create(createAirportDto.name(), createAirportDto.city(), createAirportDto.country());
        var dto = new AirportDto(airport.getId(), airport.getName(), airport.getCity(), airport.getCountry());
        return ResponseEntity.ok(dto);
    }
}
