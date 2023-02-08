package com.revok.pagoEnLineaApi.controller;

import com.revok.pagoEnLineaApi.model.Contrato;
import com.revok.pagoEnLineaApi.model.Departamento;
import com.revok.pagoEnLineaApi.model.Deuda;
import com.revok.pagoEnLineaApi.service.ContratoService;
import com.revok.pagoEnLineaApi.util.ContratoNotFound;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
                                                 @RequestParam(defaultValue = "") @NotBlank String departamento) throws ContratoNotFound {
        final String departamentoUppercase = departamento.toUpperCase();
        if (cvcontrato.length() > 10 || Long.valueOf(cvcontrato).compareTo((long) Integer.MAX_VALUE) > 0)
            return ResponseEntity.badRequest().header("error", "cvcontrato es demaciado largo").build();
        if (Arrays.stream(Departamento.values()).noneMatch(d -> d.name().equals(departamentoUppercase))) {
            return ResponseEntity.badRequest().header("error", "Departamento desconocido").build();
        }
        Departamento departamentoTyped = Departamento.valueOf(Departamento.class, departamentoUppercase);
        Contrato contrato = contratoService.findContrato(cvcontrato, departamentoTyped);
        return ResponseEntity.ok(contrato);
    }

    @GetMapping("/deuda")
    public ResponseEntity<Deuda> findDeudaContrato(@RequestParam(defaultValue = "") @NotBlank String cvcontrato,
                                                   @RequestParam(defaultValue = "") @NotBlank String departamento,
                                                   @RequestParam(defaultValue = "0") @Min(value = 0) Integer meses) {
        final String departamentoUppercase = departamento.toUpperCase();
        if (Arrays.stream(Departamento.values()).noneMatch(d -> d.name().equals(departamentoUppercase))) {
            return ResponseEntity.badRequest().header("error", "Departamento desconocido").build();
        }
        Departamento departamentoTyped = Departamento.valueOf(Departamento.class, departamentoUppercase);
        Deuda deuda = contratoService.findDeudaFromMeses(cvcontrato, departamentoTyped, meses);
        return ResponseEntity.ok(deuda);
    }

    @GetMapping("/maxDeudor")
    public ResponseEntity<Contrato> findMaxDeudaMesesContrato() {
        Contrato contrato = contratoService.maxDeuda();
        return ResponseEntity.ok(contrato);
    }
}
