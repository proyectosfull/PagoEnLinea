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
import java.util.ArrayList;
import java.util.List;
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
        List<?> result = query.getResultList();
        if (result.isEmpty())
            return null;
        return (ReferenciaBancaria) result.get(0);
    }
    public Ultimopago findUltimoPago(String cvcontrato, Boolean tieneMedidor) {
        Query query = entityManager.createNamedQuery("findUltimoPago");
        query.setParameter(1, cvcontrato);
        Ultimopago ultimopago = (Ultimopago) query.getSingleResult();
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
        ultimopago.setFechaCubre(LocalDate.parse(ultimopago.getFechaCubre(), formatterReturn).format(formatterReturn));
        return ultimopago;
    }

    public int findMesesPorPagar(String cvcontrato, Boolean tieneMedidor, String fechaCubre) {
        int mesesPorPagar;
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
        Query query = entityManager.createNamedQuery("findConceptosPago");
        query.setParameter(1, cvcontrato);
        List<?> result = query.getResultList();
        if (result.isEmpty())
            return null;
        return (Concepto) result.get(0);
    }

    public Convenio findConvenio(String cvcontrato) {
        Query query = entityManager.createNamedQuery("findConvenio");
        query.setParameter(1, cvcontrato);
        List<?> result = query.getResultList();
        if (result.isEmpty())
            return null;
        return (Convenio) result.get(0);
    }

    public List<ParcialidadConcepto> findAllParcialidadConcepto(String cvcontrato) {
        Query query = entityManager.createNamedQuery("findAllParcialidadConcepto");
        query.setParameter(1, cvcontrato);
        return (List<ParcialidadConcepto>) query.getResultList();
    }

    public String findFechaUltimoPago(String cvcontrato) {
        Query query = entityManager.createNativeQuery("SELECT TOP 1 d.fecha" +
                " FROM PagoGlobal p JOIN DetallesFacturas d ON p.numrecibo = d.numfactura" +
                " WHERE d.cvcontrato = ? AND d.cvproducto = '4' AND p.status = 'PAGADO'" +
                " ORDER BY d.fechacod DESC", String.class);
        query.setParameter(1, cvcontrato);
        List<?> result = query.getResultList();
        if (result.isEmpty())
            return null;
        return (String) result.get(0);
    }

    public Mensaje findMensaje(String cvcontrato) {
        Query query = entityManager.createNamedQuery("findMensaje");
        query.setParameter(1, cvcontrato);
        List<?> result = query.getResultList();
        if (result.isEmpty())
            return null;
        return (Mensaje) result.get(0);
    }

    public boolean existsHistorioAuxiliar(String cvcontrato) {
        Query query = entityManager.createNativeQuery(" SELECT * FROM HistoricoAuxiliar " +
                " WHERE cvcontrato = ?" +
                " AND (CONVERT(money, importe_v) + CONVERT(money, importe_a)) != 0 AND estatus='A'");
        query.setParameter(1, cvcontrato);
        return query.getResultList().size() > 0;
    }

    public List<HistoricoAuxiliar> findAllHistoricoAuxiliar(String cvcontrato) {
        if (!existsHistorioAuxiliar(cvcontrato))
            return new ArrayList<>();
        Query query = entityManager.createNamedQuery("findAllHistoricoAuxiliar");
        query.setParameter(1, cvcontrato);
        return (List<HistoricoAuxiliar>) query.getResultList();
    }

    public Toma findToma(String cvcontrato) {
        Query query = entityManager.createNamedQuery("findToma");
        query.setParameter(1, cvcontrato);
        return (Toma) query.getSingleResult();
    }

    public UltimaLectura findUltimaLectura(String cvcontrato) {
        Query query = entityManager.createNamedQuery("findUltimaLectura");
        query.setParameter(1, cvcontrato);
        List<?> result = query.getResultList();
        if (result.isEmpty())
            return null;
        return (UltimaLectura) result.get(0);
    }

    public Contrato findContrato(String cvcontrato, Departamento departamento) {
        Contrato contrato = new Contrato();
        contrato.setPropietario(findPropietario(cvcontrato));
        contrato.setFactura(findFactura(cvcontrato));
        contrato.setReferenciaBancaria(findReferenciaBancaria(cvcontrato, departamento));
        contrato.setToma(findToma(cvcontrato));
        contrato.setUltimoPago(findUltimoPago(cvcontrato, contrato.getToma().getTieneMedidor()));
        contrato.setMesesPorPagar(findMesesPorPagar(cvcontrato, contrato.getToma().getTieneMedidor(), contrato.getUltimoPago().getFechaCubre()));
        contrato.setConcepto(findConcepto(cvcontrato));
        contrato.setConvenio(findConvenio(cvcontrato));
        contrato.setParcialidadConceptos(findAllParcialidadConcepto(cvcontrato));
        contrato.setFechaUltimoPago(findFechaUltimoPago(cvcontrato));
        contrato.setMensaje(findMensaje(cvcontrato));
        if (contrato.getToma() != null && contrato.getToma().getTieneMedidor()) {
            contrato.setUltimaLectura(findUltimaLectura(cvcontrato));
        }
        contrato.setHistoricoAuxiliar(findAllHistoricoAuxiliar(cvcontrato));
        return contrato;
    }
}
