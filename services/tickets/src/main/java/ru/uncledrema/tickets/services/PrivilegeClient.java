package ru.uncledrema.tickets.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import ru.uncledrema.tickets.dto.BalanceOperationDto;
import ru.uncledrema.tickets.dto.PrivilegeShortInfoDto;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrivilegeClient {
    private final RestTemplate restTemplate;

    @Value("${upstream.gateway:http://localhost:8080/api/v1}")
    private String gatewayUrl;

    public Optional<PrivilegeShortInfoDto> getPrivilegeForUser(String username) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Name", username);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<PrivilegeShortInfoDto> response = restTemplate.exchange(
                    gatewayUrl + "/privilege",
                    HttpMethod.GET,
                    entity,
                    PrivilegeShortInfoDto.class
            );
            return Optional.ofNullable(response.getBody());
        }
        catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw ex;
        }
    }

    public Optional<PrivilegeShortInfoDto> withdrawBonuses(String username, UUID ticketUid, int amount) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Name", username);
            headers.setContentType(MediaType.APPLICATION_JSON);

            BalanceOperationDto dto = new BalanceOperationDto(ticketUid, amount);
            HttpEntity<BalanceOperationDto> entity = new HttpEntity<>(dto, headers);

            ResponseEntity<PrivilegeShortInfoDto> response = restTemplate.exchange(
                    gatewayUrl + "/privilege/withdraw",
                    HttpMethod.POST,
                    entity,
                    PrivilegeShortInfoDto.class
            );
            return Optional.ofNullable(response.getBody());
        }
        catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw ex;
        }
    }

    public Optional<PrivilegeShortInfoDto> depositBonuses(String username, UUID ticketUid, int amount) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Name", username);

            BalanceOperationDto dto = new BalanceOperationDto(ticketUid, amount);
            HttpEntity<BalanceOperationDto> entity = new HttpEntity<>(dto, headers);

            ResponseEntity<PrivilegeShortInfoDto> response = restTemplate.exchange(
                    gatewayUrl + "/privilege/deposit",
                    HttpMethod.POST,
                    entity,
                    PrivilegeShortInfoDto.class
            );
            return Optional.ofNullable(response.getBody());
        }
        catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw ex;
        }
    }

    public Optional<PrivilegeShortInfoDto> cancel(String username, UUID ticketUid) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Name", username);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<PrivilegeShortInfoDto> response = restTemplate.exchange(
                    gatewayUrl + "/privilege/cancel/" + ticketUid.toString(),
                    HttpMethod.POST,
                    entity,
                    PrivilegeShortInfoDto.class
            );
            return Optional.ofNullable(response.getBody());
        }
        catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw ex;
        }
    }
}
