package com.revok.pagoEnLineaApi.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class Deuda {
    private float tarifaMensual = 0f;
    private float saneamientoMensual = 0f;
    private float totalPagar = 0f;
    private float totalCuotaOConsumo = 0f;
    private float totalSaneamiento = 0f;
    private float totalRecargos = 0f;
    private float totalGastosCobranza = 0f;
    private float totalConvenio = 0f;
    private float saldoFavor = 0f;
    private LocalDate fechaUltimoPago;
    private LocalDate fechaCubre;
    private List<Concepto> conceptos = new ArrayList<>();
}
