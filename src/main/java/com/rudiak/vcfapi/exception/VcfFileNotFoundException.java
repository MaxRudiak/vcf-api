package com.rudiak.vcfapi.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class VcfFileNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 526685239473761492L;

    public VcfFileNotFoundException(String message) {
        super(message);
    }
}
