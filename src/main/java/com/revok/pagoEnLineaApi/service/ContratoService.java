package com.revok.pagoEnLineaApi.service;

import com.revok.pagoEnLineaApi.model.Factura;
import com.revok.pagoEnLineaApi.model.Propietario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContratoService {
    private final EntityManager entityManager;

    public Propietario findPropietario(String cvcontrato) {
        Query query = entityManager.createNamedQuery("findPropietario");
        query.setParameter(1, cvcontrato);
        return (Propietario) query.getSingleResult();
    }

    public Factura findFactura(String cvcontrato) {
        Query query = entityManager.createNamedQuery("findFactura");
        query.setParameter(1, cvcontrato);
        return (Factura) query.getSingleResult();
    }
}
