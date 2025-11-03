package ru.uncledrema.tickets.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpServerErrorException;

@ControllerAdvice
@ResponseBody
public class RestExceptionHandler {

    @ExceptionHandler(HttpServerErrorException.ServiceUnavailable.class)
    public ResponseEntity<String> handleServiceUnavailable(HttpServerErrorException.ServiceUnavailable ex) {
        String body = ex.getResponseBodyAsString();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(!body.isEmpty() ? body : ex.getMessage(), headers, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
