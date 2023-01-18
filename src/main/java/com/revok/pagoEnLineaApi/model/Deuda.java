package com.revok.pagoEnLineaApi.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Deuda {
    private BigDecimal tarifaMensual = BigDecimal.ZERO;
    private BigDecimal saneamientoMensual = BigDecimal.ZERO;
    private BigDecimal totalPagar = BigDecimal.ZERO;
    private BigDecimal totalCuotaOConsumo = BigDecimal.ZERO;
    private BigDecimal totalSaneamiento = BigDecimal.ZERO;
    private BigDecimal totalRecargos = BigDecimal.ZERO;
    private BigDecimal totalGastosCobranza = BigDecimal.ZERO;
    private LocalDate fechaUltimoPago;
    private LocalDate fechaCubre;
    private List<Concepto> conceptos = new ArrayList<>();
}
