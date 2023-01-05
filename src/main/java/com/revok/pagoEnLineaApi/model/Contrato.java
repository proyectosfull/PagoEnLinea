package com.revok.pagoEnLineaApi.model;

import lombok.Data;

@Data
public class Contrato {
    private Factura factura;
    private Propietario propietario;
}
