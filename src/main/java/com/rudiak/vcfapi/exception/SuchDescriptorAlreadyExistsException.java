package com.rudiak.vcfapi.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SuchDescriptorAlreadyExistsException extends RuntimeException {
    private static final long serialVersionUID = -8266549820693191669L;

    public SuchDescriptorAlreadyExistsException(String messsage) {
        super(messsage);
    }
}
