package com.revok.pagoEnLineaApi.controller;

import com.revok.pagoEnLineaApi.model.Contrato;
import com.revok.pagoEnLineaApi.model.Departamento;
import com.revok.pagoEnLineaApi.model.Deuda;
import com.revok.pagoEnLineaApi.service.ContratoService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/contrato")
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;

    @GetMapping
    public ResponseEntity<Contrato> findContrato(@RequestParam(defaultValue = "") @NotBlank String cvcontrato,
                                                 @RequestParam(defaultValue = "") @NotBlank String departamento) {
        if (Arrays.stream(Departamento.values()).noneMatch(d -> d.name().equals(departamento.toUpperCase()))) {
            return ResponseEntity.badRequest().header("error", "Departamento desconocido").build();
        }
        Contrato contrato = contratoService.findContrato(cvcontrato, Departamento.valueOf(Departamento.class, departamento.toUpperCase()));
        return ResponseEntity.ok(contrato);
    }

    @GetMapping("/deuda")
    public ResponseEntity<Deuda> findDeudaContrato(@RequestParam(defaultValue = "") @NotBlank String cvcontrato,
                                                   @RequestParam(defaultValue = "") @NotBlank String departamento,
                                                   @RequestParam(defaultValue = "0") @NotNull Integer meses) {
        if (Arrays.stream(Departamento.values()).noneMatch(d -> d.name().equals(departamento.toUpperCase()))) {
            return ResponseEntity.badRequest().header("error", "Departamento desconocido").build();
        }

        Contrato contrato = contratoService.findContrato(cvcontrato, Departamento.valueOf(Departamento.class, departamento.toUpperCase()));
        Deuda deuda = contratoService.findCuentaPorPagarFromMonths(contrato, meses, Departamento.valueOf(Departamento.class, departamento.toUpperCase()));
        return ResponseEntity.ok(deuda);
    }
}
