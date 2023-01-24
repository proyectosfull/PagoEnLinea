package com.revok.pagoEnLineaApi.model;

import lombok.Data;

import java.util.List;

@Data
public class Contrato {

    private String cvcontrato;
    private Propietario propietario;
    private Factura factura;
    private String referenciaBancaria;
    private Toma toma;
    private Ultimopago ultimoPago;
    private int mesesPorPagar;
    private List<Concepto> conceptos;
    private Convenio convenio;
    private List<ParcialidadConcepto> parcialidadConceptos;
    private UltimaLectura ultimaLectura;
    private Mensaje mensaje;
    private List<HistoricoAuxiliar> historicoAuxiliar;
}
