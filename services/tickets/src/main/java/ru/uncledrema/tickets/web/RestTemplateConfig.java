package ru.uncledrema.tickets.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class RestTemplateConfig {

    private static final String HEADER_NAME = "X-Service-Key";
    private static final String HEADER_VALUE = "tickets";

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        ClientHttpRequestInterceptor serviceKeyInterceptor = (request, body, execution) -> {
            request.getHeaders().add(HEADER_NAME, HEADER_VALUE);
            return execution.execute(request, body);
        };

        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
        interceptors.add(serviceKeyInterceptor);
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }
}
