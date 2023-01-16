package com.revok.pagoEnLineaApi.service;

import com.revok.pagoEnLineaApi.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ContratoService {
    private final EntityManager entityManager;
    private final ZoneId defaultZoneId = ZoneId.of("America/Mexico_City");
    private final Locale defaultLocale = new Locale("spa", "MX");
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", defaultLocale);
    private final DateTimeFormatter formatterReturn = DateTimeFormatter.ofPattern("dd/MM/yyyy", defaultLocale);

    public Contrato maxDeuda() {
        Query query = entityManager.createNativeQuery("SELECT cvcontrato FROM dattomas", String.class);
        @SuppressWarnings("unchecked")
        List<String> cvcontratos = query.getResultList();
        System.out.println("totalContratos: " + cvcontratos.size());
        int i = 0;
        Contrato contratoMax = new Contrato();
        contratoMax.setMesesPorPagar(0);
        for (String cvcontrato : cvcontratos) {
            System.out.println("i: " + i);
            i++;
            Contrato contrato = new Contrato();
            contrato.setCvcontrato(cvcontrato);
            contrato.setToma(findToma(cvcontrato));
            contrato.setUltimoPago(findUltimoPago(cvcontrato, contrato.getToma().getTieneMedidor()));
            if (contrato.getUltimoPago() == null)
                continue;
            try {
                contrato.setMesesPorPagar(findMesesPorPagar(cvcontrato, contrato.getToma().getTieneMedidor(), contrato.getUltimoPago().getFechaCubre()));
                if (contratoMax.getMesesPorPagar() < contrato.getMesesPorPagar())
                    contratoMax = contrato;
            } catch (Exception ignored) {
            }
        }
        System.out.println("max cvcontrato: " + contratoMax.getCvcontrato());
        System.out.println("max meses: " + contratoMax.getMesesPorPagar());
        return contratoMax;
    }

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

    public String findReferenciaBancaria(String cvcontrato, Departamento departamento) {
        if (departamento != Departamento.CENTRO)
            return null;
        Query query = entityManager.createNativeQuery(
                "SELECT TOP 1 CONCAT(digitosLibres, digitosFecha, digitosImporte, digitosReferencia) AS referencia" +
                        " FROM HistorialReferenciasCIE" +
                        " WHERE cvcontrato = ?" +
                        " AND CONVERT(date, fechaVigencia) >= ?", String.class);
        query.setParameter(1, cvcontrato);
        query.setParameter(2, LocalDate.now(defaultZoneId).format(formatter));
        List<?> result = query.getResultList();

        // return null if result list or object result string is empty
        return result.isEmpty() ? null : ((String) result.get(0)).isEmpty() ? null : (String) result.get(0);
    }

    public Ultimopago findUltimoPago(String cvcontrato, Boolean tieneMedidor) {
        Query query = entityManager.createNamedQuery("findUltimoPago");
        query.setParameter(1, cvcontrato);
        List<?> resultQueryUltimoPago = query.getResultList();
        Ultimopago ultimopago = resultQueryUltimoPago.isEmpty() ? null : (Ultimopago) resultQueryUltimoPago.get(0);
        if (ultimopago == null)
            return null;

        Query queryFechaCubreFromLecturas = entityManager.createNativeQuery(
                "SELECT TOP 1 feclectura" +
                        " FROM lecturas" +
                        " WHERE cvcontrato = ? AND cvstatus='PAGADO'" +
                        " ORDER BY fcodlectura desc");

        Query queryFechaCubreFromTomas = entityManager.createNativeQuery(
                "SELECT TOP 1 fechacubre" +
                        " FROM ControlTomas" +
                        " WHERE cvcontrato = ?");

        // si tiene medidor, la fecha de cobertura se obtiene de las lecturas
        // de lo contrario, se obtiene del control de tomas
        Query queryFechaCubre = tieneMedidor ? queryFechaCubreFromLecturas : queryFechaCubreFromTomas;
        queryFechaCubre.setParameter(1, cvcontrato);
        List<?> result = queryFechaCubre.getResultList();
        ultimopago.setFechaCubre((String) (result.isEmpty() ? null : result.get(0)));


        // si el último pago no existe o incluye fecha default, se aplica fecha de cobertura del control de tomas
        if (ultimopago.getFechaCubre() == null || ultimopago.getFechaCubre().isEmpty() || ultimopago.getFechaCubre().equals("0/0/0")) {
            queryFechaCubre = queryFechaCubreFromTomas;
            queryFechaCubre.setParameter(1, cvcontrato);
            result = queryFechaCubre.getResultList();
            ultimopago.setFechaCubre((String) (result.isEmpty() ? null : result.get(0)));
        }

        // se aplica formato de fecha al dato antes de retornarlo
        //* se estima que el aplicar el mismo formato es innecesario. comprobar
        // ultimopago.setFechaCubre(LocalDate.parse(ultimopago.getFechaCubre(), formatterReturn).format(formatterReturn));
        return ultimopago;
    }

    public int findMesesPorPagar(String cvcontrato, Boolean tieneMedidor, String fechaCubre) {
        if (!tieneMedidor) {
            // distance from fechaCubre and now in months
            return (int) ChronoUnit.MONTHS.between(YearMonth.parse(fechaCubre, formatterReturn), YearMonth.now(defaultZoneId));
        }
        Query query = entityManager.createNativeQuery("SELECT COUNT(*) FROM lecturas WHERE cvcontrato = ? AND cvstatus = 'POR PAGAR'", Integer.class);
        query.setParameter(1, cvcontrato);
        List<?> result = query.getResultList();
        return result.isEmpty() ? 0 : (int) result.get(0);
    }

    @SuppressWarnings("unchecked")
    public List<Concepto> findAllConcepto(String cvcontrato) {
        Query query = entityManager.createNamedQuery("findAllConcepto");
        query.setParameter(1, cvcontrato);
        return query.getResultList();
    }

    public Convenio findConvenio(String cvcontrato) {
        Query query = entityManager.createNamedQuery("findConvenio");
        query.setParameter(1, cvcontrato);
        List<?> result = query.getResultList();
        return result.isEmpty() ? null : (Convenio) result.get(0);
    }

    @SuppressWarnings("unchecked")
    public List<ParcialidadConcepto> findAllParcialidadConcepto(String cvcontrato) {
        Query query = entityManager.createNamedQuery("findAllParcialidadConcepto");
        query.setParameter(1, cvcontrato);
        return query.getResultList();
    }

    public String findFechaUltimoPago(String cvcontrato) {
        Query query = entityManager.createNativeQuery("SELECT TOP 1 d.fecha" +
                " FROM PagoGlobal p JOIN DetallesFacturas d ON p.numrecibo = d.numfactura" +
                " WHERE d.cvcontrato = ? AND d.cvproducto = '4' AND p.status = 'PAGADO'" +
                " ORDER BY d.fechacod DESC", String.class);
        query.setParameter(1, cvcontrato);
        List<?> result = query.getResultList();
        return result.isEmpty() ? null : (String) result.get(0);
    }

    public Mensaje findMensaje(String cvcontrato) {
        Query query = entityManager.createNamedQuery("findMensaje");
        query.setParameter(1, cvcontrato);
        List<?> result = query.getResultList();
        return result.isEmpty() ? null : (Mensaje) result.get(0);
    }

    public boolean existsHistorioAuxiliar(String cvcontrato) {
        Query query = entityManager.createNativeQuery(" SELECT cvcontrato FROM HistoricoAuxiliar " +
                " WHERE cvcontrato = ?" +
                " AND (CONVERT(money, importe_v) + CONVERT(money, importe_a)) != 0 AND estatus='A'");
        query.setParameter(1, cvcontrato);
        return !query.getResultList().isEmpty();
    }

    @SuppressWarnings("unchecked")
    public List<HistoricoAuxiliar> findAllHistoricoAuxiliar(String cvcontrato) {
        if (!existsHistorioAuxiliar(cvcontrato))
            return new ArrayList<>();
        Query query = entityManager.createNamedQuery("findAllHistoricoAuxiliar");
        query.setParameter(1, cvcontrato);
        return query.getResultList();
    }

    public Toma findToma(String cvcontrato) {
        Query query = entityManager.createNamedQuery("findToma");
        query.setParameter(1, cvcontrato);
        List<?> result = query.getResultList();
        return result.isEmpty() ? null : (Toma) result.get(0);
    }

    public UltimaLectura findUltimaLectura(String cvcontrato) {
        Query query = entityManager.createNamedQuery("findUltimaLectura");
        query.setParameter(1, cvcontrato);
        List<?> result = query.getResultList();
        return result.isEmpty() ? null : (UltimaLectura) result.get(0);
    }

    public Contrato findContrato(String cvcontrato, Departamento departamento) {
        Contrato contrato = new Contrato();
        contrato.setCvcontrato(cvcontrato);
        contrato.setPropietario(findPropietario(cvcontrato));
        contrato.setFactura(findFactura(cvcontrato));
        contrato.setReferenciaBancaria(findReferenciaBancaria(cvcontrato, departamento));
        contrato.setToma(findToma(cvcontrato));
        contrato.setUltimoPago(findUltimoPago(cvcontrato, contrato.getToma().getTieneMedidor()));
        contrato.setMesesPorPagar(findMesesPorPagar(cvcontrato, contrato.getToma().getTieneMedidor(), contrato.getUltimoPago().getFechaCubre()));
        contrato.setConceptos(findAllConcepto(cvcontrato));
        contrato.setConvenio(findConvenio(cvcontrato));
        contrato.setParcialidadConceptos(findAllParcialidadConcepto(cvcontrato));
        contrato.setFechaUltimoPago(findFechaUltimoPago(cvcontrato));
        contrato.setMensaje(findMensaje(cvcontrato));
        if (contrato.getToma() != null && contrato.getToma().getTieneMedidor())
            contrato.setUltimaLectura(findUltimaLectura(cvcontrato));
        contrato.setHistoricoAuxiliar(findAllHistoricoAuxiliar(cvcontrato));
        return contrato;
    }

    @SuppressWarnings("unchecked")
    public List<Parametro> findAllParametro() {
        return entityManager.createNamedQuery("findAllParametro").getResultList();
    }

    public Deuda findCuentaPorPagarFromMonths(Contrato contrato, Departamento departamento, int meses) {
        // if meses comes with 0 then set mesesPorPagar from contrato
        meses = meses > 0 ? meses : contrato.getMesesPorPagar();

        List<Parametro> parametros = findAllParametro();
        // assert parametros values exists
        if (parametros.isEmpty())
            return null;
        Parametro parametroRecargo = parametros.stream().filter(p -> p.getCvparam().equals(Parametro.ParametroType.RECA.name())).findAny().orElse(null);
        Parametro parametroMesesGastosCobransa = parametros.stream().filter(p -> p.getCvparam().equals(Parametro.ParametroType.MESESGE.name())).findAny().orElse(null);
        Parametro parametroGastosCobransa = parametros.stream().filter(p -> p.getCvparam().equals(Parametro.ParametroType.GE.name())).findAny().orElse(null);

        int mesesDeuda = contrato.getMesesPorPagar();

        float giroTarifaAnterior = contrato.getToma().getTarifaAnterior() * contrato.getToma().getNumfamilia();
        float giroTarifaActual = contrato.getToma().getTarifaActual() * contrato.getToma().getNumfamilia();
        float giroSaneamientoActual = contrato.getToma().getTarifaConSaneamiento();

        giroTarifaAnterior *= contrato.getToma().getNumfamilia();
        giroTarifaActual *= contrato.getToma().getNumfamilia();
        LocalDate fechaUltimoPago = LocalDate.parse(contrato.getUltimoPago().getFechaCubre(), formatterReturn);

        int mesesConRecargos = mesesDeuda - 1;

        float maxRecargos = 0;


        if (mesesConRecargos > 0) {
            if (mesesDeuda <= mesesConRecargos) {
                mesesConRecargos = mesesDeuda;
            }
            maxRecargos = calcularMaxRangos(
                    fechaUltimoPago,
                    LocalDate.parse(contrato.getToma().getFechaVigenciaTarifaGiro(), formatterReturn),
                    giroTarifaAnterior,
                    giroTarifaActual
            );
        }

        BigDecimal totalCuotabd = new BigDecimal("0");
        float totalCuota = 0f;
        float totalSaneamiento = 0f;
        BigDecimal totalSaneamientobd = new BigDecimal("0");
        float totalGastosCobranza = 0f;
        float totalRecargos = 0f;

        float tarifa = 0f;
        float saneamiento = 0f;

        // empieza bloque de cuota fija

        if (departamento == Departamento.CENTRO) {
            Query query = entityManager.createNamedQuery("findUltimoAnioVigencia");
            query.setParameter(1, contrato.getToma().getCvgiro());
            UltimoAnioVigencia ultimoAnioVigencia = (UltimoAnioVigencia) query.getSingleResult();
            for (int i = 1; i <= meses; i++) {
                // Obtener el valor de la tarifa del historial de acuerdo a la fecha.
                Query queryTarifa = entityManager.createNativeQuery(" SELECT TOP 1 tagua_actual" +
                        " FROM tarifas WHERE cvgiro = ? AND YEAR(fechavigencia) = ?" +
                        " ORDER BY CONVERT(date, fechavigencia) DESC", Float.class);
                queryTarifa.setParameter(1, contrato.getToma().getCvgiro());
                queryTarifa.setParameter(2,
                        (fechaUltimoPago.getYear() < ultimoAnioVigencia.getMinVigenciaTarifa() ?
                                ultimoAnioVigencia.getMinVigenciaTarifa() :
                                (fechaUltimoPago.getYear() > ultimoAnioVigencia.getMaxVigenciaTarifa() ?
                                        ultimoAnioVigencia.getMaxVigenciaTarifa() : fechaUltimoPago.getYear()))
                );
                List<?> queryTarifaResult = queryTarifa.getResultList();
                tarifa = queryTarifaResult.isEmpty() ? 0f : (float) queryTarifaResult.get(0);
                totalCuotabd = totalCuotabd.add(BigDecimal.valueOf(tarifa).setScale(2, RoundingMode.HALF_DOWN));

                Query querySaneamiento = entityManager.createNativeQuery(" SELECT TOP 1 tsane_actual" +
                        " FROM tarifas WHERE cvgiro = ?" +
                        " AND YEAR(fsanevigencia) = ?"
                        + " ORDER BY CONVERT(date, fsanevigencia) DESC", Float.class);
                querySaneamiento.setParameter(1, contrato.getToma().getCvgiro());
                querySaneamiento.setParameter(2,
                        (fechaUltimoPago.getYear() < ultimoAnioVigencia.getMinVigenciaSaneamiento() ?
                                ultimoAnioVigencia.getMinVigenciaSaneamiento() :
                                (fechaUltimoPago.getYear() > ultimoAnioVigencia.getMaxVigenciaSaneamiento() ?
                                        ultimoAnioVigencia.getMaxVigenciaSaneamiento() : fechaUltimoPago.getYear()))
                );

                List<?> querySaneamientoResult = querySaneamiento.getResultList();
                saneamiento = querySaneamientoResult.isEmpty() ? 0f : (float) querySaneamientoResult.get(0);
                totalSaneamientobd = totalSaneamientobd.add(BigDecimal.valueOf(saneamiento).setScale(2, RoundingMode.HALF_DOWN));
                // Si el contador es  menor a la cantidad de meses total, se agregan recargos
                // (cuando sea igual a los meses de deuda se asimila que es el
                // mes corriente y no se agrega recargo).
                if (i < mesesDeuda && !contrato.getToma().getEspecial() && parametroRecargo != null) {
                    totalRecargos = BigDecimal.valueOf(totalRecargos).setScale(2, RoundingMode.HALF_DOWN)
                            .add(BigDecimal.valueOf(tarifa).setScale(2, RoundingMode.HALF_DOWN)
                                    .add(BigDecimal.valueOf(saneamiento).setScale(2, RoundingMode.HALF_DOWN))
                                    .multiply(BigDecimal.valueOf(parametroRecargo.getCantidad())))
                            .setScale(2, RoundingMode.HALF_DOWN)
                            .floatValue();
                }
                fechaUltimoPago = fechaUltimoPago.plus(1, ChronoUnit.MONTHS);
            }
        } else {
            for (int i = 0; i < meses; i++) {
                // Obtener el valor de la tarifa actual del giro.
                tarifa = giroTarifaActual;
                totalCuotabd = totalCuotabd.add(BigDecimal.valueOf(tarifa).setScale(2, RoundingMode.HALF_DOWN));

                // Obtener el valor del saneamiento actual del giro.
                saneamiento = giroSaneamientoActual;
                totalSaneamientobd = totalSaneamientobd.add(BigDecimal.valueOf(saneamiento).setScale(2, RoundingMode.HALF_DOWN));

                if (i < mesesDeuda && !contrato.getToma().getEspecial() && parametroRecargo != null) {
                    totalRecargos = BigDecimal.valueOf(totalRecargos).setScale(2, RoundingMode.HALF_DOWN)
                            .add(BigDecimal.valueOf(tarifa).setScale(2, RoundingMode.HALF_DOWN)
                                    .add(BigDecimal.valueOf(saneamiento).setScale(2, RoundingMode.HALF_DOWN))
                                    .multiply(BigDecimal.valueOf(parametroRecargo.getCantidad())))
                            .setScale(2, RoundingMode.HALF_DOWN)
                            .floatValue();
                }
            }
        }
        totalCuota = totalCuotabd.floatValue();
        totalSaneamiento = totalSaneamientobd.floatValue();
        // Si los meses vencidos son mayor o igual al de máximo de gastos de cobranza
        // se agregan a la deuda.
        if (parametroMesesGastosCobransa != null && mesesConRecargos >= parametroMesesGastosCobransa.getCantidad()) {
            // Obtener meses con gastos de cobranza restando a los meses de adeudo la cantidad
            // de meses en la que se comienza a cobrar esos gastos de cobranza.
            int mesesConGastosCobranza = mesesDeuda - parametroMesesGastosCobransa.getCantidad().intValue();
            // Obtener los meses a pagar que no incluyen gastos de cobranza.
            int mesesPagarSinGastosCobranza = meses - mesesConGastosCobranza;
            // Si paga más de con gastos de cobranza, se quitan l

            float tarifaConGastosCobranza = meses <= mesesConGastosCobranza ? totalCuota :
                    totalCuotabd.subtract(
                            BigDecimal.valueOf(tarifa).setScale(2, RoundingMode.HALF_DOWN)
                                    .multiply(BigDecimal.valueOf(mesesPagarSinGastosCobranza))
                    ).floatValue();
            float saneamientoConGastosCobranza = meses <= mesesConGastosCobranza ? totalSaneamiento :
                    totalSaneamientobd.subtract(
                            BigDecimal.valueOf(saneamiento).setScale(2, RoundingMode.HALF_DOWN)
                                    .multiply(BigDecimal.valueOf(mesesPagarSinGastosCobranza))
                    ).floatValue();

            if (parametroGastosCobransa != null && !contrato.getToma().getEspecial()) {
                totalGastosCobranza =
                        BigDecimal.valueOf(tarifaConGastosCobranza).setScale(2, RoundingMode.HALF_DOWN)
                                .add(BigDecimal.valueOf(saneamientoConGastosCobranza).setScale(2, RoundingMode.HALF_DOWN))
                                .add(BigDecimal.valueOf(totalRecargos).setScale(2, RoundingMode.HALF_DOWN))
                                .multiply(BigDecimal.valueOf(parametroGastosCobransa.getCantidad()))
                                .setScale(2, RoundingMode.HALF_DOWN)
                                .floatValue();
            }
        }


        // termina bloque de cuota fija

        Deuda deuda = new Deuda();
        Concepto conceptoParticular;

        // verificar si descuento de 12x11 esta activo; aplicarlo en caso de estar activo

        // buscar concepto de  descuento por campaña promocional anual en caso de estar activo.
        // valor nulo en caso contrario

        conceptoParticular = findConceptoDescuento12x11(contrato, fechaUltimoPago);

        if (conceptoParticular != null) {
            deuda.getConceptos().add(conceptoParticular);
            deuda.setTotalPagar(
                    BigDecimal.valueOf(deuda.getTotalPagar()).setScale(2, RoundingMode.HALF_DOWN)
                            .add(BigDecimal.valueOf(conceptoParticular.getCosto()).setScale(2, RoundingMode.HALF_DOWN))
                            .floatValue()
            );
        }

        if (!contrato.getToma().getEspecial()) {
            conceptoParticular = new Concepto();
            conceptoParticular.setCvconcepto(contrato.getToma().getTieneMedidor() ? 3 : 5);
            conceptoParticular.setDescripcion(contrato.getToma().getTieneMedidor() ? "CONSUMO" : "CUOTA FIJA");
            conceptoParticular.setCosto(totalCuota);
            deuda.getConceptos().add(conceptoParticular);

            if (contrato.getToma().getSaneamiento()) {
                conceptoParticular = new Concepto();
                conceptoParticular.setCvconcepto(19);
                conceptoParticular.setDescripcion("SANEAMIENTO");
                conceptoParticular.setCosto(totalSaneamiento);
                deuda.getConceptos().add(conceptoParticular);
            }

            if (totalRecargos > 0) {
                conceptoParticular = new Concepto();
                conceptoParticular.setCvconcepto(17);
                conceptoParticular.setDescripcion("RECARGOS");
                conceptoParticular.setCosto(totalRecargos);
                deuda.getConceptos().add(conceptoParticular);

                conceptoParticular = findConceptoDescuentoRecargos(totalRecargos, departamento);
                if (conceptoParticular != null) {
                    deuda.getConceptos().add(conceptoParticular);
                    deuda.setTotalPagar(
                            BigDecimal.valueOf(deuda.getTotalPagar()).setScale(2, RoundingMode.HALF_DOWN)
                                    .add(BigDecimal.valueOf(conceptoParticular.getCosto()).setScale(2, RoundingMode.HALF_DOWN))
                                    .floatValue()
                    );
                }
            }

            if (totalGastosCobranza > 0) {
                conceptoParticular = new Concepto();
                conceptoParticular.setCvconcepto(8);
                conceptoParticular.setDescripcion("GASTOS DE COBRANZA");
                conceptoParticular.setCosto(totalGastosCobranza);
                deuda.getConceptos().add(conceptoParticular);

                conceptoParticular = findConceptoDescuentoGastosCobranza(totalGastosCobranza, departamento);
                if (conceptoParticular != null) {
                    deuda.getConceptos().add(conceptoParticular);
                    deuda.setTotalPagar(
                            BigDecimal.valueOf(deuda.getTotalPagar()).setScale(2, RoundingMode.HALF_DOWN)
                                    .add(BigDecimal.valueOf(conceptoParticular.getCosto()).setScale(2, RoundingMode.HALF_DOWN))
                                    .floatValue()
                    );
                }
            }

            List<Concepto> conceptos = findAllConcepto(contrato.getCvcontrato());
            deuda.getConceptos().addAll(conceptos);
            for (Concepto concepto : conceptos) {
                deuda.setTotalPagar(
                        BigDecimal.valueOf(deuda.getTotalPagar()).setScale(2, RoundingMode.HALF_DOWN)
                                .add(BigDecimal.valueOf(concepto.getCosto()).setScale(2, RoundingMode.HALF_DOWN))
                                .floatValue()
                );
            }

        } else {
            float totalConsumoCuota;
            List<Concepto> conceptos = findAllConcepto(contrato.getCvcontrato());
            totalConsumoCuota = conceptos.stream().map(Concepto::getCosto).reduce(Float::sum).orElse(0f);
            totalConsumoCuota += contrato.getConvenio().getPorPagar();
            conceptoParticular = new Concepto();
            conceptoParticular.setCvconcepto(contrato.getToma().getTieneMedidor() ? 3 : 5);
            conceptoParticular.setDescripcion(contrato.getToma().getTieneMedidor() ? "CONSUMO" : "CUOTA FIJA");
            conceptoParticular.setCosto(
                    BigDecimal.valueOf(totalCuota).setScale(2, RoundingMode.HALF_DOWN)
                            .add(BigDecimal.valueOf(totalConsumoCuota).setScale(2, RoundingMode.HALF_DOWN))
                            .add(BigDecimal.valueOf(totalRecargos).setScale(2, RoundingMode.HALF_DOWN))
                            .add(BigDecimal.valueOf(totalGastosCobranza).setScale(2, RoundingMode.HALF_DOWN))
                            .floatValue()
            );
            conceptos.add(conceptoParticular);

            if (contrato.getToma().getSaneamiento()) {
                conceptoParticular = new Concepto();
                conceptoParticular.setCvconcepto(19);
                conceptoParticular.setDescripcion("SANEAMIENTO");
                conceptoParticular.setCosto(deuda.getTotalSaneamiento());
                deuda.getConceptos().add(conceptoParticular);
            }
        }

        Query querySaldoAFavor = entityManager.createNamedQuery("findSaldoAFavorConcepto");
        querySaldoAFavor.setParameter(1, contrato.getCvcontrato());
        List<?> querySaldoAFavorResult = querySaldoAFavor.getResultList();
        if (!querySaldoAFavorResult.isEmpty()) {
            conceptoParticular = (Concepto) querySaldoAFavorResult.get(0);
            deuda.getConceptos().add(conceptoParticular);
            deuda.setTotalPagar(
                    BigDecimal.valueOf(deuda.getTotalPagar()).setScale(2, RoundingMode.HALF_DOWN)
                            .add(BigDecimal.valueOf(conceptoParticular.getCosto()).setScale(2, RoundingMode.HALF_DOWN))
                            .floatValue()
            );
        }


        deuda.setTarifaMensual(contrato.getToma().getTarifaActual());
        deuda.setSaneamientoMensual(contrato.getToma().getSaneamientoGiro());
        deuda.setTotalGastosCobranza(totalGastosCobranza);
        deuda.setTotalRecargos(totalRecargos);
        deuda.setTotalSaneamiento(totalSaneamiento);
        deuda.setTotalCuotaOConsumo(totalCuota);
        deuda.setTotalPagar(
                BigDecimal.valueOf(deuda.getTotalPagar()).setScale(2, RoundingMode.HALF_DOWN)
                        .add(BigDecimal.valueOf(totalCuota).setScale(2, RoundingMode.HALF_DOWN))
                        .add(BigDecimal.valueOf(totalSaneamiento).setScale(2, RoundingMode.HALF_DOWN))
                        .add(BigDecimal.valueOf(totalRecargos).setScale(2, RoundingMode.HALF_DOWN))
                        .add(BigDecimal.valueOf(totalGastosCobranza).setScale(2, RoundingMode.HALF_DOWN))
                        .floatValue()
        );
        deuda.setFechaCubre(fechaUltimoPago);
        deuda.setFechaUltimoPago(LocalDate.parse(contrato.getUltimoPago().getFechaCubre(), formatterReturn));
        return deuda;
    }

    private Concepto findConceptoDescuento12x11(Contrato contrato, LocalDate fechaCubre) {
        float tarifaMensual = BigDecimal.valueOf(contrato.getToma().getTarifaActual()).setScale(2, RoundingMode.HALF_DOWN)
                .multiply(BigDecimal.valueOf(contrato.getToma().getNumfamilia())).floatValue();
        float saneamientoMensual = contrato.getToma().getSaneamientoGiro();
        Query queryDescuento12x11 = entityManager.createNativeQuery("SELECT CASE WHEN activapromocion = 'SI' THEN 1 ELSE 0 END AS activapromocion, fechadetiene FROM datosagua");
        @SuppressWarnings("unchecked")
        List<Object[]> resultQueryDescuento12x11 = queryDescuento12x11.getResultList();
        if (resultQueryDescuento12x11.isEmpty()) {
            return null;
        }

        boolean isPromocionActiva = ((int) resultQueryDescuento12x11.get(0)[0]) == 1;
        String fechaFinPromocion = (String) resultQueryDescuento12x11.get(0)[1];
        if (!isPromocionActiva) {
            return null;
        }

        Query queryAplicaDiciembre = entityManager.createNativeQuery("SELECT CASE WHEN aplicadiciembre = 'SI' THEN 1 ELSE 0 END" +
                " FROM giros WHERE cvgiros = ?");
        queryAplicaDiciembre.setParameter(1, contrato.getToma().getCvgiro());
        List<?> resultQueryAplicaDiciembre = (List<?>) queryAplicaDiciembre.getResultList();
        if (resultQueryAplicaDiciembre.isEmpty() || ((int) resultQueryAplicaDiciembre.get(0)) == 0) {
            return null;
        }

        Query queryIsAreaComun = entityManager.createNativeQuery(" SELECT COUNT(*) AS counter" +
                " FROM DatClientes c JOIN DatTomas t ON c.cvcliente = t.cvcliente" +
                " WHERE t.cvcontrato = ?" +
                " AND CONCAT(nombre, ' ', appaterno, ' ', apmaterno) LIKE '%AREA COMUN%'");
        queryIsAreaComun.setParameter(1, contrato.getCvcontrato());
        List<?> resultQueryEsAreaComun = queryIsAreaComun.getResultList();

        //no aplicable a contrato perteneciente a AREA COMUN
        if (!resultQueryEsAreaComun.isEmpty() && ((int) resultQueryEsAreaComun.get(0)) > 0) {
            return null;
        }

        // apply LocalDate type and set year and month to max values (end of year)
        LocalDate fechaFinPromocionTyped = LocalDate.parse(fechaFinPromocion, formatterReturn);
        fechaFinPromocionTyped = LocalDate.of(fechaFinPromocionTyped.getYear(), 12, 31);
        if (fechaCubre.getMonth() != fechaFinPromocionTyped.getMonth() || fechaCubre.getYear() != fechaFinPromocionTyped.getYear()) {
            return null;
        }

        // obtener descripcion del concepto DESCUENTO ANUAL
        Query queryDescuentoAnual = entityManager.createNativeQuery("SELECT descripcion FROM conceptospago WHERE cvconcepto='70'");
        List<?> resultQueryDescuentoAnual = queryDescuentoAnual.getResultList();
        String nombrePromocion = resultQueryDescuentoAnual.isEmpty() ? "" : (String) resultQueryDescuentoAnual.get(0);

        // Concepto de descuento 12x11
        Concepto concepto = new Concepto();
        concepto.setCvconcepto(70);
        concepto.setDescripcion(nombrePromocion);
        concepto.setCosto(
                BigDecimal.valueOf(tarifaMensual).setScale(2, RoundingMode.HALF_DOWN)
                        .add(BigDecimal.valueOf(saneamientoMensual).setScale(2, RoundingMode.HALF_DOWN))
                        .floatValue() * -1f
        );
        return concepto;
    }


    private Concepto findConceptoDescuentoRecargos(float totalRecargos, Departamento departamento) {
        if (departamento != Departamento.CENTRO)
            return null;
        Query queryIsPromotionActive = entityManager.createNativeQuery("SELECT TOP 1" +
                " CASE WHEN promtodo = 'SI' THEN 1 ELSE 0 END" +
                " FROM Datosagua", Integer.class);
        List<?> resultIsPromotionActive = queryIsPromotionActive.getResultList();
        if (resultIsPromotionActive.isEmpty() || ((int) resultIsPromotionActive.get(0)) == 0)
            return null;
        Concepto conceptoPromotion = new Concepto();
        conceptoPromotion.setCvconcepto(91);
        conceptoPromotion.setDescripcion("DESC. 100% DE RECARGOS");
        conceptoPromotion.setCosto(totalRecargos * -1);
        return conceptoPromotion;
    }

    private Concepto findConceptoDescuentoGastosCobranza(float totalGastos, Departamento departamento) {
        if (departamento != Departamento.CENTRO)
            return null;
        Query queryIsPromotionActive = entityManager.createNativeQuery("SELECT TOP 1" +
                " CASE WHEN promtodo = 'SI' THEN 1 ELSE 0 END" +
                " FROM Datosagua", Integer.class);
        List<?> resultIsPromotionActive = queryIsPromotionActive.getResultList();
        if (resultIsPromotionActive.isEmpty() || ((int) resultIsPromotionActive.get(0)) == 0)
            return null;
        Concepto conceptoPromotion = new Concepto();
        conceptoPromotion.setCvconcepto(92);
        conceptoPromotion.setDescripcion("DESC. 50% DE GASTOS DE COBRANZA");
        conceptoPromotion.setCosto(
                BigDecimal.valueOf(totalGastos).setScale(2, RoundingMode.HALF_DOWN)
                        .multiply(new BigDecimal("0.5"))
                        .setScale(2, RoundingMode.HALF_DOWN)
                        .negate()
                        .floatValue()
        );
        return conceptoPromotion;
    }

    private float calcularMaxRangos(LocalDate fechaUltimoPago, LocalDate fechaVigente, float tarifaAnterior, float tarifaActual) {
        if (fechaUltimoPago.isBefore(fechaVigente)) {
            int mesesEntrePagoYVigente = (int) ChronoUnit.MONTHS.between(
                    YearMonth.from(fechaUltimoPago),
                    YearMonth.from(fechaVigente)
            );
            int mesesConTarifaAnterior = mesesEntrePagoYVigente - 1;
            int mesesEntreVigenteYAhora = (int) ChronoUnit.MONTHS.between(
                    YearMonth.from(fechaVigente),
                    YearMonth.from(LocalDate.now())
            );
            int mesesConTarifaActual = mesesEntreVigenteYAhora - 1;
            return BigDecimal.valueOf(tarifaAnterior).setScale(2, RoundingMode.HALF_DOWN)
                    .multiply(BigDecimal.valueOf(mesesConTarifaAnterior))
                    .add(BigDecimal.valueOf(tarifaActual).setScale(2, RoundingMode.HALF_DOWN)
                            .multiply(BigDecimal.valueOf(mesesConTarifaActual)))
                    .multiply(new BigDecimal("2"))
                    .setScale(2, RoundingMode.HALF_DOWN)
                    .abs()
                    .floatValue();
        }
        int mesesEntrePagoYAhora = (int) ChronoUnit.MONTHS.between(
                YearMonth.from(fechaUltimoPago),
                YearMonth.from(LocalDate.now())
        );
        int mesesConTarifaActual = mesesEntrePagoYAhora - 1;
        return BigDecimal.valueOf(tarifaActual).setScale(2, RoundingMode.HALF_DOWN)
                .multiply(BigDecimal.valueOf(mesesConTarifaActual))
                .setScale(2, RoundingMode.HALF_DOWN)
                .abs()
                .floatValue();
    }
}
