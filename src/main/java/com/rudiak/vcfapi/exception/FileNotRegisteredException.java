package com.rudiak.vcfapi.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FileNotRegisteredException extends RuntimeException {
    private static final long serialVersionUID = 3234304826391447967L;

    public FileNotRegisteredException(String message) {
        super(message);
    }
}
