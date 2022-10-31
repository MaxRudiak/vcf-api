package com.rudiak.vcfapi.controller;

import com.rudiak.vcfapi.exception.FileNotRegisteredException;
import com.rudiak.vcfapi.exception.InvalidRequestException;
import com.rudiak.vcfapi.exception.SuchDescriptorAlreadyExistsException;
import com.rudiak.vcfapi.exception.VcfFileNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class ControllerAdvisor extends ResponseEntityExceptionHandler {

    @ExceptionHandler(FileNotRegisteredException.class)
    public ResponseEntity<Object> fileNotRegisteredExceptionHandler(
            FileNotRegisteredException exception, WebRequest request
    ) {
        return handleExceptionInternal(exception, createBody(exception, HttpStatus.NOT_FOUND),
                new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<Object> invalidRequestExceptionHandler(
            InvalidRequestException exception, WebRequest request
    ) {
        return handleExceptionInternal(exception, createBody(exception, HttpStatus.BAD_REQUEST),
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(VcfFileNotFoundException.class)
    public ResponseEntity<Object> vcfFileNotFoundExceptionHandler(
            VcfFileNotFoundException exception, WebRequest request
    ) {
        return handleExceptionInternal(exception, createBody(exception, HttpStatus.NOT_FOUND),
                new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(SuchDescriptorAlreadyExistsException.class)
    public ResponseEntity<Object> suchDescriptorAlreadyExistsHandler(
            SuchDescriptorAlreadyExistsException exception, WebRequest request
    ) {
        return handleExceptionInternal(exception, createBody(exception, HttpStatus.CONFLICT),
                new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    private Map<String, Object> createBody(final RuntimeException exception, final HttpStatus status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp:", LocalDateTime.now());
        body.put("status", status);
        body.put("message", exception.getMessage());
        return body;
    }
}
