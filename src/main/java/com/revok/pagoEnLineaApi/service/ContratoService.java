package com.revok.pagoEnLineaApi.service;

import com.revok.pagoEnLineaApi.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ContratoService {
    private final EntityManager entityManager;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", new Locale("spa", "MX"));
    private final DateTimeFormatter formatterReturn = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("spa", "MX"));

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

    public ReferenciaBancaria findReferenciaBancaria(String cvcontrato, Departamento departamento) {
        if (departamento != Departamento.CENTRO)
            return new ReferenciaBancaria();
        Query query = entityManager.createNamedQuery("findReferenciaBancaria");
        query.setParameter(1, cvcontrato);
        query.setParameter(2, LocalDate.now().format(formatter));
        return (ReferenciaBancaria) query.getSingleResult();
    }

    public Ultimopago findUltimoPago(String cvcontrato, Boolean tieneMedidor) {
        Query query = entityManager.createNamedQuery("findUltimoPago");
        query.setParameter(1, cvcontrato);
        Ultimopago ultimopago = (Ultimopago) query.getSingleResult();
        Query queryFechaCubre =
                tieneMedidor ? entityManager.createNamedQuery("findFechaCubreFromLecturas") :
                        entityManager.createNamedQuery("findFechaCubreFromControlTomas");
        queryFechaCubre.setParameter(1, cvcontrato);
        ultimopago.setFechaCubre((String) queryFechaCubre.getSingleResult());
        if (ultimopago.getFechaCubre().equals("0/0/0") || ultimopago.getFechaCubre().equals(""))
            ultimopago.setFechaCubre((String) entityManager.createNamedQuery("findFechaCubreFromControlTomas").getSingleResult());
        ultimopago.setFechaCubre(LocalDate.parse(ultimopago.getFechaCubre()).format(formatterReturn));
        return ultimopago;
    }

    public int findMesesPorPagar(String cvcontrato, Boolean tieneMedidor, String fechaCubre) {
        int mesesPorPagar = 0;
        if (tieneMedidor) {
            Query query = entityManager.createNativeQuery("SELECT COUNT(*) FROM lecturas WHERE cvcontrato = ? AND cvstatus = 'POR PAGAR'");
            query.setParameter(1, cvcontrato);
            mesesPorPagar = (int) query.getSingleResult();
        }
        else {
            LocalDate now = LocalDate.now();
            LocalDate fechaCubreTyped = LocalDate.parse(fechaCubre, formatterReturn);
            mesesPorPagar = (int) ChronoUnit.MONTHS.between(
                    YearMonth.from(now),
                    YearMonth.from(fechaCubreTyped)
            );
        }
        return mesesPorPagar;
    }
}
