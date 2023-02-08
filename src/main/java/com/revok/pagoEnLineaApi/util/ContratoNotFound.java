package com.revok.pagoEnLineaApi.util;

import lombok.Getter;

@Getter
public class ContratoNotFound extends Exception {
    private final String cvcontrato;

    public ContratoNotFound(String cvcontrato, String message) {
        super(message);
        this.cvcontrato = cvcontrato;
    }
}
