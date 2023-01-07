package com.revok.pagoEnLineaApi.controller;

import com.revok.pagoEnLineaApi.model.Contrato;
import com.revok.pagoEnLineaApi.model.Departamento;
import com.revok.pagoEnLineaApi.service.ContratoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contrato")
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;

    @GetMapping
    public ResponseEntity<Contrato> findContrato(@RequestParam(defaultValue = "") String cvcontrato,
                                                 @RequestParam(defaultValue = "") String departamento) {
        if (cvcontrato.equals("") || departamento.equals(""))
            return ResponseEntity.badRequest()
                    .header("error", "parametros no incluidos en solicitud").build();
        Contrato contrato = contratoService.findContrato(cvcontrato, Departamento.valueOf(Departamento.class, departamento.toUpperCase()));
        return ResponseEntity.ok(contrato);
    }
}
