package com.rudiak.vcfapi.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InvalidRequestException extends RuntimeException {
    private static final long serialVersionUID = 5923256730108559279L;

    public InvalidRequestException(String message) {
        super(message);
    }
}
