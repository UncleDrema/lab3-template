package ru.uncledrema.gateway.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.uncledrema.gateway.dto.PrivilegeShortInfoDto;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrivilegeClient {
    private final RestTemplate restTemplate;

    @Value("${downstream.privileges:http://localhost:8050}")
    private String serviceUrl;

    public Optional<PrivilegeShortInfoDto> getPrivilegeForUser(String username) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Name", username);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<PrivilegeShortInfoDto> response = restTemplate.exchange(
                    serviceUrl + "/privilege",
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
}
