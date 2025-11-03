package ru.uncledrema.tickets.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.uncledrema.tickets.dto.FlightDto;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FlightClient {
    private final RestTemplate restTemplate;

    @Value("${upstream.gateway:http://localhost:8080/api/v1}")
    private String gatewayUrl;

    public Optional<FlightDto> getFlight(String flightNumber) {
        try {
            var flight = restTemplate.getForObject(gatewayUrl + "/flights/" + flightNumber, FlightDto.class);
            return Optional.ofNullable(flight);
        }
        catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw ex;
        }
    }
}
