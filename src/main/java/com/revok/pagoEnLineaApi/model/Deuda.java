package com.revok.pagoEnLineaApi.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Deuda {
    private BigDecimal tarifaMensual = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
    private BigDecimal saneamientoMensual = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
    private BigDecimal totalPagar = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
    private BigDecimal totalCuotaOConsumo = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
    private BigDecimal totalSaneamiento = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
    private BigDecimal totalRecargos = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
    private BigDecimal totalGastosCobranza = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
    private LocalDate fechaUltimoPago;
    private LocalDate fechaCubre;
    private List<Concepto> conceptos = new ArrayList<>();
}
