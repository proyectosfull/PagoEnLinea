package com.revok.pagoEnLineaApi.service;

import com.revok.pagoEnLineaApi.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Ref;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ContratoService {
    private final EntityManager entityManager;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", new Locale("spa", "MX"));
    private final DateTimeFormatter formatterReturn = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("spa", "MX"));

    public Propietario findPropietario(String cvcontrato) {
        TypedQuery<Propietario> query = entityManager.createNamedQuery("findPropietario", Propietario.class);
        query.setParameter(1, cvcontrato);
        return query.getSingleResult();
    }

    public Factura findFactura(String cvcontrato) {
        TypedQuery<Factura> query = entityManager.createNamedQuery("findFactura", Factura.class);
        query.setParameter(1, cvcontrato);
        return query.getSingleResult();
    }

    public ReferenciaBancaria findReferenciaBancaria(String cvcontrato, Departamento departamento) {
        if (departamento != Departamento.CENTRO)
            return new ReferenciaBancaria();
        TypedQuery<ReferenciaBancaria> query = entityManager.createNamedQuery("findReferenciaBancaria", ReferenciaBancaria.class);
        query.setParameter(1, cvcontrato);
        query.setParameter(2, LocalDate.now().format(formatter));
        return query.getSingleResult();
    }

    public Ultimopago findUltimoPago(String cvcontrato, Boolean tieneMedidor) {
        TypedQuery<Ultimopago> query = entityManager.createNamedQuery("findUltimoPago", Ultimopago.class);
        query.setParameter(1, cvcontrato);
        Ultimopago ultimopago = query.getSingleResult();
        // si tiene medidor, la fecha de cobertura se obtiene de las lecturas
        // de lo contrario, se obtiene del control de tomas
        Query queryFechaCubre =
                tieneMedidor ? entityManager.createNamedQuery("findFechaCubreFromLecturas") :
                        entityManager.createNamedQuery("findFechaCubreFromControlTomas");
        queryFechaCubre.setParameter(1, cvcontrato);
        ultimopago.setFechaCubre((String) queryFechaCubre.getSingleResult());

        // si el último pago no existe o incluye fecha default, se aplica fecha de cobertura del control de tomas
        if (ultimopago.getFechaCubre().equals("0/0/0") || ultimopago.getFechaCubre().equals(""))
            ultimopago.setFechaCubre((String) entityManager.createNamedQuery("findFechaCubreFromControlTomas").getSingleResult());
        // se aplica formato de fecha al dato antes de retornarlo
        ultimopago.setFechaCubre(LocalDate.parse(ultimopago.getFechaCubre()).format(formatterReturn));
        return ultimopago;
    }

    public int findMesesPorPagar(String cvcontrato, Boolean tieneMedidor, String fechaCubre) {
        int mesesPorPagar = 0;
        if (tieneMedidor) {
            Query query = entityManager.createNativeQuery("SELECT COUNT(*) FROM lecturas WHERE cvcontrato = ? AND cvstatus = 'POR PAGAR'", Integer.class);
            query.setParameter(1, cvcontrato);
            mesesPorPagar = (int) query.getSingleResult();
        } else {
            LocalDate now = LocalDate.now();
            LocalDate fechaCubreTyped = LocalDate.parse(fechaCubre, formatterReturn);
            mesesPorPagar = (int) ChronoUnit.MONTHS.between(
                    YearMonth.from(now),
                    YearMonth.from(fechaCubreTyped)
            );
        }
        return mesesPorPagar;
    }

    public Concepto findConcepto(String cvcontrato) {
        TypedQuery<Concepto> query = entityManager.createNamedQuery("findConceptosPago", Concepto.class);
        query.setParameter(1, cvcontrato);
        return query.getSingleResult();
    }

    public Convenio findConvenio(String cvcontrato) {
        TypedQuery<Convenio> query = entityManager.createNamedQuery("findConvenio", Convenio.class);
        query.setParameter(1, cvcontrato);
        return query.getSingleResult();
    }

    public List<ParcialidadConcepto> findAllParcialidadConcepto(String cvcontrato) {
        TypedQuery<ParcialidadConcepto> query = entityManager.createNamedQuery("findAllParcialidadConcepto", ParcialidadConcepto.class);
        query.setParameter(1, cvcontrato);
        return query.getResultList();
    }

    public String findFechaUltimoPago(String cvcontrato) {
        Query query = entityManager.createNativeQuery("SELECT TOP 1 d.fecha" +
                " FROM PagoGlobal p JOIN DetallesFacturas d ON p.numrecibo = d.numfactura" +
                " WHERE d.cvcontrato = ? AND d.cvproducto = '4' AND p.status = 'PAGADO'" +
                " ORDER BY d.fechacod DESC", String.class);
        query.setParameter(1, cvcontrato);
        return (String) query.getSingleResult();
    }

    public Mensaje findMensaje(String cvcontrato) {
        TypedQuery<Mensaje> query = entityManager.createNamedQuery("findMensaje", Mensaje.class);
        return query.getSingleResult();
    }
}
