package com.revok.pagoEnLineaApi.model;

import lombok.Data;

import java.util.List;

@Data
public class Contrato {
    private Propietario propietario;
    private Factura factura;
    private ReferenciaBancaria referenciaBancaria;
    private Toma toma;
    private Ultimopago ultimoPago;
    private int mesesPorPagar;
    private Concepto concepto;
    private Convenio convenio;
    private List<ParcialidadConcepto> parcialidadConceptos;
    private String fechaUltimoPago;
    private UltimaLectura ultimaLectura;
    private Mensaje mensaje;
    private List<HistoricoAuxiliar> historicoAuxiliar;
}
