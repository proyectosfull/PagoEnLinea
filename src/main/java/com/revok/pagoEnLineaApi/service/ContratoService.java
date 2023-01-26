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
import java.time.temporal.TemporalAdjusters;
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

        Query queryFechaCubre = entityManager.createNativeQuery("SELECT TOP 1 fechacubre FROM ControlTomas WHERE cvcontrato = ?", LocalDate.class);
        ;
        queryFechaCubre.setParameter(1, cvcontrato);
        List<?> result = queryFechaCubre.getResultList();
        ultimopago.setFechaCubre((LocalDate) (result.isEmpty() ? null : result.get(0)));
        return ultimopago;
    }

    public int findMesesPorPagar(String cvcontrato, Boolean tieneMedidor, LocalDate fechaCubre) {
        if (!tieneMedidor) {
            // distance between fechaCubre and now in months
            return (int) ChronoUnit.MONTHS.between(YearMonth.from(fechaCubre), YearMonth.now(defaultZoneId));
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

    public Deuda findDeudaFromMeses(String cvcontrato, Departamento departamento, int meses) {
        Contrato contrato = new Contrato();
        contrato.setCvcontrato(cvcontrato);
        contrato.setToma(findToma(cvcontrato));
        contrato.setUltimoPago(findUltimoPago(cvcontrato, contrato.getToma().getTieneMedidor()));
        contrato.setMesesPorPagar(findMesesPorPagar(cvcontrato, contrato.getToma().getTieneMedidor(), contrato.getUltimoPago().getFechaCubre()));

        // if meses comes with 0 then set mesesPorPagar from contrato
        meses = meses > 0 ? meses : contrato.getMesesPorPagar();

        Deuda deuda = contrato.getToma().getTieneMedidor() ? getDeudaFromMedidor(contrato, meses) : getDeudaFromCuotaFija(contrato, departamento, meses);

        appendDefaultConceptos(deuda, contrato, departamento, meses);
        return deuda;
    }

    private Deuda getDeudaFromMedidor(Contrato contrato, int meses) {
        TriFunction<Integer, String, BigDecimal, BigDecimal> getTarifaFromConsumo = (giro, tipo, consumo) -> {
            BigDecimal totalLectura = BigDecimal.ZERO;
            BigDecimal UMA = new BigDecimal("96.22");

            if (tipo.equals("SANEAMIENTO"))
                consumo = consumo.multiply(new BigDecimal("0.75"));

            Query query = entityManager.createNativeQuery("SELECT valor FROM rangosconsumo WHERE clave = " +
                    "(SELECT cvclasifica FROM giros WHERE cvgiros = ?)" +
                    " AND tipo = ? AND CONVERT(FLOAT, ?) BETWEEN rangoInicial AND rangoFinal", BigDecimal.class);
            query.setParameter(1, giro);
            query.setParameter(2, tipo);
            query.setParameter(3, consumo.toPlainString());

            @SuppressWarnings("unchecked")
            List<BigDecimal> queryResult = query.getResultList();

            for (BigDecimal valor : queryResult) {
                BigDecimal valorPorUma = valor.multiply(UMA);
                BigDecimal consumoPorValorPorUma = consumo.multiply(valorPorUma);
                totalLectura = totalLectura.add(consumoPorValorPorUma);
            }

            return totalLectura;
        };

        List<Parametro> parametros = findAllParametro();
        // assert parametros values exists
        if (parametros.isEmpty())
            return null;
        Parametro parametroRecargo = parametros.stream().filter(p -> p.getCvparam().equals(Parametro.ParametroType.RECA.name())).findAny().orElse(null);
        Parametro parametroMesesGastosCobransa = parametros.stream().filter(p -> p.getCvparam().equals(Parametro.ParametroType.MESESGE.name())).findAny().orElse(null);
        Parametro parametroGastosCobransa = parametros.stream().filter(p -> p.getCvparam().equals(Parametro.ParametroType.GE.name())).findAny().orElse(null);

        if (parametroRecargo == null || parametroGastosCobransa == null || parametroMesesGastosCobransa == null)
            return null;

        BigDecimal totalVencido = BigDecimal.ZERO;
        BigDecimal totalAdeudo = BigDecimal.ZERO;

        BigDecimal totalConsumo = BigDecimal.ZERO;
        BigDecimal totalSaneamiento = BigDecimal.ZERO;
        BigDecimal totalGastosCobranza = BigDecimal.ZERO;
        BigDecimal totalRecargos = BigDecimal.ZERO;

        Query queryFechaUltimaLectura = entityManager.createNativeQuery(
                "SELECT TOP 1 feclectura FROM lecturas WHERE cvcontrato = ? ORDER BY fcodlectura DESC", LocalDate.class);
        queryFechaUltimaLectura.setParameter(1, contrato.getCvcontrato());

        List<?> resultFechaUltimaLectura = queryFechaUltimaLectura.getResultList();

        if (resultFechaUltimaLectura.isEmpty())
            return null;

        LocalDate fechaUltimaLectura = (LocalDate) resultFechaUltimaLectura.get(0);
        LocalDate fechaUltimaLecturaLastDayOfMonth = fechaUltimaLectura.with(TemporalAdjusters.lastDayOfMonth());

        Query queryLecturas = entityManager.createNativeQuery(
                "SELECT feclectura, consumo FROM lecturas WHERE cvcontrato = ? AND cvstatus = 'POR PAGAR' ORDER BY fcodlectura DESC");
        queryLecturas.setParameter(1, contrato.getCvcontrato());

        @SuppressWarnings("unchecked")
        List<Object[]> resultLecturas = queryLecturas.getResultList();
        if (resultLecturas.isEmpty())
            return null;

        final LocalDate now = LocalDate.now(defaultZoneId);
        final LocalDate nowMinusCobertura = now.minus(1, ChronoUnit.MONTHS);
        final LocalDate nowMinusCoberturaAndGastos = now.minus(2, ChronoUnit.MONTHS);

        for (int i = 0; i < meses; i++) {
            Object[] record = resultLecturas.get(i);
            LocalDate fechaLectura = LocalDate.parse((String) record[0], formatter);
            BigDecimal consumo = BigDecimal.valueOf((int) record[1]);
            boolean isExpired = false;
            boolean isOutlay = false;

            BigDecimal tarifaFromConsumo = getTarifaFromConsumo.apply(contrato.getToma().getCvgiro(), "AGUA", consumo);
            totalConsumo = totalConsumo.add(tarifaFromConsumo);
            totalAdeudo = totalAdeudo.add(tarifaFromConsumo);

            if (fechaLectura.isBefore(fechaUltimaLecturaLastDayOfMonth.minus(1, ChronoUnit.MONTHS)) &&
                    fechaLectura.isBefore(nowMinusCobertura)) {
                totalVencido = totalVencido.add(tarifaFromConsumo);
                isExpired = true;
            }

            if (fechaLectura.isBefore(fechaUltimaLecturaLastDayOfMonth.minus(2, ChronoUnit.MONTHS)) &&
                    fechaLectura.isBefore(nowMinusCoberturaAndGastos)) {
                totalGastosCobranza = totalGastosCobranza.add(tarifaFromConsumo);
                isOutlay = true;
            }

            if (!contrato.getToma().getSaneamiento())
                continue;

            BigDecimal tarifaFromSaneamiento = getTarifaFromConsumo.apply(contrato.getToma().getCvgiro(), "SANEAMIENTO", consumo);
            totalSaneamiento = totalSaneamiento.add(tarifaFromSaneamiento);
            totalAdeudo = totalAdeudo.add(tarifaFromSaneamiento);

            if (isExpired) {
                totalVencido = totalVencido.add(tarifaFromSaneamiento);
            }
            if (isOutlay) {
                totalGastosCobranza = totalGastosCobranza.add(tarifaFromSaneamiento);
            }
        }
        if (contrato.getToma().getEspecial()) {
            totalGastosCobranza = BigDecimal.ZERO;
        } else {
            totalRecargos = totalVencido.multiply(BigDecimal.valueOf(parametroRecargo.getCantidad()));
            if (totalGastosCobranza.compareTo(BigDecimal.ZERO) > 0) {
                totalGastosCobranza = totalGastosCobranza.multiply(BigDecimal.valueOf(parametroGastosCobransa.getCantidad()));
            }
        }

        Deuda deuda = new Deuda();
        deuda.setTotalGastosCobranza(totalGastosCobranza.setScale(2, RoundingMode.HALF_DOWN));
        deuda.setTotalRecargos(totalRecargos.setScale(2, RoundingMode.HALF_DOWN));
        deuda.setTotalSaneamiento(totalSaneamiento.setScale(2, RoundingMode.HALF_DOWN));
        deuda.setTotalCuotaOConsumo(totalConsumo.setScale(2, RoundingMode.HALF_DOWN));
        deuda.setTotalPagar(totalConsumo.add(totalSaneamiento).add(totalRecargos).add(totalGastosCobranza).setScale(2, RoundingMode.HALF_DOWN));
        deuda.setFechaCubre(contrato.getUltimoPago().getFechaCubre());
        deuda.setFechaUltimoPago(contrato.getUltimoPago().getFechaRegistro());
        return deuda;
    }

    private Deuda getDeudaFromCuotaFija(Contrato contrato, Departamento departamento, int meses) {
        int mesesDeuda = contrato.getMesesPorPagar();
        int mesesConRecargos = mesesDeuda - 1;
        BigDecimal giroTarifaAnterior = contrato.getToma().getTarifaAnterior().multiply(BigDecimal.valueOf(contrato.getToma().getNumfamilia()));
        BigDecimal giroTarifaActual = contrato.getToma().getTarifaActual().multiply(BigDecimal.valueOf(contrato.getToma().getNumfamilia()));
        BigDecimal giroSaneamientoActual = contrato.getToma().getTarifaConSaneamiento();

        LocalDate fechaUltimoPago = contrato.getUltimoPago().getFechaCubre();

        BigDecimal maxRecargos = mesesConRecargos > 0 ? calcularMaxRangos(
                fechaUltimoPago,
                contrato.getToma().getFechaVigenciaTarifaGiro(),
                giroTarifaAnterior,
                giroTarifaActual
        ) : BigDecimal.ZERO;

        BigDecimal totalCuota = BigDecimal.ZERO;
        BigDecimal totalSaneamiento = BigDecimal.ZERO;
        BigDecimal totalGastosCobranza = BigDecimal.ZERO;
        BigDecimal totalRecargos = BigDecimal.ZERO;
        BigDecimal tarifa = BigDecimal.ZERO;
        BigDecimal saneamiento = BigDecimal.ZERO;

        List<Parametro> parametros = findAllParametro();
        // assert parametros values exists
        if (parametros.isEmpty())
            return null;
        Parametro parametroRecargo = parametros.stream().filter(p -> p.getCvparam().equals(Parametro.ParametroType.RECA.name())).findAny().orElse(null);
        Parametro parametroMesesGastosCobransa = parametros.stream().filter(p -> p.getCvparam().equals(Parametro.ParametroType.MESESGE.name())).findAny().orElse(null);
        Parametro parametroGastosCobransa = parametros.stream().filter(p -> p.getCvparam().equals(Parametro.ParametroType.GE.name())).findAny().orElse(null);

        if (departamento == Departamento.CENTRO) {
            Query query = entityManager.createNamedQuery("findUltimoAnioVigencia");
            query.setParameter(1, contrato.getToma().getCvgiro());
            UltimoAnioVigencia ultimoAnioVigencia = (UltimoAnioVigencia) query.getSingleResult();
            for (int i = 1; i <= meses; i++) {
                // Obtener el valor de la tarifa del historial de acuerdo a la fecha.
                Query queryTarifa = entityManager.createNativeQuery(" SELECT TOP 1 tagua_actual" +
                        " FROM tarifas WHERE cvgiro = ? AND YEAR(fechavigencia) = ?" +
                        " ORDER BY CONVERT(date, fechavigencia) DESC", BigDecimal.class);
                queryTarifa.setParameter(1, contrato.getToma().getCvgiro());
                queryTarifa.setParameter(2,
                        (fechaUltimoPago.getYear() < ultimoAnioVigencia.getMinVigenciaTarifa() ?
                                ultimoAnioVigencia.getMinVigenciaTarifa() :
                                (fechaUltimoPago.getYear() > ultimoAnioVigencia.getMaxVigenciaTarifa() ?
                                        ultimoAnioVigencia.getMaxVigenciaTarifa() : fechaUltimoPago.getYear()))
                );
                List<?> queryTarifaResult = queryTarifa.getResultList();
                tarifa = queryTarifaResult.isEmpty() ? BigDecimal.ZERO : (BigDecimal) queryTarifaResult.get(0);
                totalCuota = totalCuota.add(tarifa);

                Query querySaneamiento = entityManager.createNativeQuery(" SELECT TOP 1 tsane_actual" +
                        " FROM tarifas WHERE cvgiro = ?" +
                        " AND YEAR(fsanevigencia) = ?"
                        + " ORDER BY CONVERT(date, fsanevigencia) DESC", BigDecimal.class);
                querySaneamiento.setParameter(1, contrato.getToma().getCvgiro());
                querySaneamiento.setParameter(2,
                        (fechaUltimoPago.getYear() < ultimoAnioVigencia.getMinVigenciaSaneamiento() ?
                                ultimoAnioVigencia.getMinVigenciaSaneamiento() :
                                (fechaUltimoPago.getYear() > ultimoAnioVigencia.getMaxVigenciaSaneamiento() ?
                                        ultimoAnioVigencia.getMaxVigenciaSaneamiento() : fechaUltimoPago.getYear()))
                );

                List<?> querySaneamientoResult = querySaneamiento.getResultList();
                saneamiento = querySaneamientoResult.isEmpty() ? BigDecimal.ZERO : (BigDecimal) querySaneamientoResult.get(0);
                totalSaneamiento = totalSaneamiento.add(saneamiento);

                // Si el contador es menor a la cantidad de meses total, se agregan recargos
                // (cuando sea igual a los meses de deuda se asimila que es el
                // mes corriente y no se agrega recargo).

                if (i < mesesDeuda && !contrato.getToma().getEspecial() && totalRecargos.compareTo(maxRecargos) < 0 && parametroRecargo != null) {
                    totalRecargos = totalRecargos
                            .add(tarifa.add(saneamiento).multiply(BigDecimal.valueOf(parametroRecargo.getCantidad())))
                            .setScale(2, RoundingMode.HALF_DOWN);
                }
                fechaUltimoPago = fechaUltimoPago.plus(1, ChronoUnit.MONTHS);
            }
        } else {
            for (int i = 0; i < meses; i++) {
                // Obtener el valor de la tarifa actual del giro.
                tarifa = giroTarifaActual;
                totalCuota = totalCuota.add(tarifa);

                // Obtener el valor del saneamiento actual del giro.
                saneamiento = giroSaneamientoActual;
                totalSaneamiento = totalSaneamiento.add(saneamiento);

                if (i < mesesDeuda && !contrato.getToma().getEspecial() && parametroRecargo != null) {
                    totalRecargos = totalRecargos
                            .add(tarifa)
                            .add(saneamiento)
                            .multiply(BigDecimal.valueOf(parametroRecargo.getCantidad()))
                            .setScale(2, RoundingMode.HALF_DOWN);
                }
            }
        }

        // Si los meses vencidos son mayor o igual al de máximo de gastos de cobranza
        // se agregan a la deuda.
        if (parametroMesesGastosCobransa != null && mesesConRecargos >= parametroMesesGastosCobransa.getCantidad()) {
            // Obtener meses con gastos de cobranza restando a los meses de adeudo la cantidad
            // de meses en la que se comienza a cobrar esos gastos de cobranza.
            int mesesConGastosCobranza = mesesDeuda - parametroMesesGastosCobransa.getCantidad().intValue();
            // Obtener los meses a pagar que no incluyen gastos de cobranza.
            int mesesPagarSinGastosCobranza = meses - mesesConGastosCobranza;
            // Si paga más de con gastos de cobranza, se quitan l

            BigDecimal tarifaConGastosCobranza = meses <= mesesConGastosCobranza ? totalCuota :
                    totalCuota.subtract(tarifa.multiply(BigDecimal.valueOf(mesesPagarSinGastosCobranza)));

            BigDecimal saneamientoConGastosCobranza = meses <= mesesConGastosCobranza ? totalSaneamiento :
                    totalSaneamiento.subtract(saneamiento.multiply(BigDecimal.valueOf(mesesPagarSinGastosCobranza)));

            if (parametroGastosCobransa != null && !contrato.getToma().getEspecial()) {
                totalGastosCobranza = tarifaConGastosCobranza
                        .add(saneamientoConGastosCobranza)
                        .add(totalRecargos)
                        .multiply(BigDecimal.valueOf(parametroGastosCobransa.getCantidad()))
                        .setScale(2, RoundingMode.HALF_DOWN);
            }
        }

        Deuda deuda = new Deuda();
        deuda.setTarifaMensual(contrato.getToma().getTarifaActual());
        deuda.setSaneamientoMensual(contrato.getToma().getSaneamientoGiro());
        deuda.setTotalGastosCobranza(totalGastosCobranza);
        deuda.setTotalRecargos(totalRecargos);
        deuda.setTotalSaneamiento(totalSaneamiento);
        deuda.setTotalCuotaOConsumo(totalCuota);
        deuda.setTotalPagar(deuda.getTotalPagar().add(totalCuota).add(totalSaneamiento).add(totalRecargos).add(totalGastosCobranza));
        deuda.setFechaCubre(contrato.getUltimoPago().getFechaCubre());
        deuda.setFechaUltimoPago(contrato.getUltimoPago().getFechaRegistro());
        return deuda;
    }

    private void appendDefaultConceptos(Deuda deuda, Contrato contrato, Departamento departamento, int meses) {
        Concepto conceptoParticular;

        // Verificar si descuento de 12x11 está activo; aplicarlo en caso de estar activo
        // Buscar concepto de descuento por campaña promocional anual en caso de estar activo.
        // Valor nulo en caso contrario
        conceptoParticular = findConceptoDescuento12x11(contrato, meses);

        if (conceptoParticular != null) {
            deuda.getConceptos().add(conceptoParticular);
            deuda.setTotalPagar(deuda.getTotalPagar().add(conceptoParticular.getCosto()));
        }

        if (!contrato.getToma().getEspecial()) {
            conceptoParticular = new Concepto();
            conceptoParticular.setCvconcepto(contrato.getToma().getTieneMedidor() ? 3 : 5);
            conceptoParticular.setDescripcion(contrato.getToma().getTieneMedidor() ? "CONSUMO" : "CUOTA FIJA");
            conceptoParticular.setCosto(deuda.getTotalCuotaOConsumo());
            deuda.getConceptos().add(conceptoParticular);

            if (contrato.getToma().getSaneamiento()) {
                conceptoParticular = new Concepto();
                conceptoParticular.setCvconcepto(19);
                conceptoParticular.setDescripcion("SANEAMIENTO");
                conceptoParticular.setCosto(deuda.getTotalSaneamiento());
                deuda.getConceptos().add(conceptoParticular);
            }

            if (deuda.getTotalRecargos().compareTo(BigDecimal.ZERO) > 0) {
                conceptoParticular = new Concepto();
                conceptoParticular.setCvconcepto(17);
                conceptoParticular.setDescripcion("RECARGOS");
                conceptoParticular.setCosto(deuda.getTotalRecargos());
                deuda.getConceptos().add(conceptoParticular);

                conceptoParticular = findConceptoDescuentoRecargos(deuda.getTotalRecargos(), departamento);
                if (conceptoParticular != null) {
                    deuda.getConceptos().add(conceptoParticular);
                    deuda.setTotalPagar(deuda.getTotalPagar().add(conceptoParticular.getCosto()));
                }
            }

            if (deuda.getTotalGastosCobranza().compareTo(BigDecimal.ZERO) > 0) {
                conceptoParticular = new Concepto();
                conceptoParticular.setCvconcepto(8);
                conceptoParticular.setDescripcion("GASTOS DE COBRANZA");
                conceptoParticular.setCosto(deuda.getTotalGastosCobranza());
                deuda.getConceptos().add(conceptoParticular);

                conceptoParticular = findConceptoDescuentoGastosCobranza(deuda.getTotalGastosCobranza(), departamento);
                if (conceptoParticular != null) {
                    deuda.getConceptos().add(conceptoParticular);
                    deuda.setTotalPagar(deuda.getTotalPagar().add(conceptoParticular.getCosto()));
                }
            }

            List<Concepto> conceptos = findAllConcepto(contrato.getCvcontrato());
            deuda.getConceptos().addAll(conceptos);
            for (Concepto concepto : conceptos) {
                deuda.setTotalPagar(deuda.getTotalPagar().add(concepto.getCosto()));
            }

        } else {
            BigDecimal totalConsumoCuota;
            List<Concepto> conceptos = findAllConcepto(contrato.getCvcontrato());
            totalConsumoCuota = conceptos.stream().map(Concepto::getCosto).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            totalConsumoCuota = totalConsumoCuota.add(contrato.getConvenio().getPorPagar());
            conceptoParticular = new Concepto();
            conceptoParticular.setCvconcepto(contrato.getToma().getTieneMedidor() ? 3 : 5);
            conceptoParticular.setDescripcion(contrato.getToma().getTieneMedidor() ? "CONSUMO" : "CUOTA FIJA");
            conceptoParticular.setCosto(deuda.getTotalCuotaOConsumo().add(totalConsumoCuota).add(deuda.getTotalRecargos()).add(deuda.getTotalGastosCobranza()));
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
            deuda.setTotalPagar(deuda.getTotalPagar().add(conceptoParticular.getCosto()));
        }
    }

    private Concepto findConceptoDescuento12x11(Contrato contrato, int meses) {
        LocalDate fechaCubreMax = contrato.getUltimoPago().getFechaCubre().plus(meses, ChronoUnit.MONTHS);
        BigDecimal tarifaMensual = contrato.getToma().getTarifaActual().multiply(BigDecimal.valueOf(contrato.getToma().getNumfamilia()));
        BigDecimal saneamientoMensual = contrato.getToma().getSaneamientoGiro();
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
        LocalDate fechaFinPromocionTyped = LocalDate.parse(fechaFinPromocion, formatter);
        fechaFinPromocionTyped = LocalDate.of(fechaFinPromocionTyped.getYear(), 12, 31);
        if (fechaCubreMax.getMonth() != fechaFinPromocionTyped.getMonth() || fechaCubreMax.getYear() != fechaFinPromocionTyped.getYear()) {
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
        concepto.setCosto(tarifaMensual.add(saneamientoMensual).negate());
        return concepto;
    }

    private Concepto findConceptoDescuentoRecargos(BigDecimal totalRecargos, Departamento departamento) {
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
        conceptoPromotion.setCosto(totalRecargos.negate());
        return conceptoPromotion;
    }

    private Concepto findConceptoDescuentoGastosCobranza(BigDecimal totalGastos, Departamento departamento) {
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
        conceptoPromotion.setCosto(totalGastos.multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_DOWN).negate());
        return conceptoPromotion;
    }

    private BigDecimal calcularMaxRangos(LocalDate fechaUltimoPago, LocalDate fechaVigente, BigDecimal tarifaAnterior, BigDecimal tarifaActual) {
        if (fechaUltimoPago.isBefore(fechaVigente)) {
            int mesesEntrePagoYVigente = (int) ChronoUnit.MONTHS.between(
                    YearMonth.from(fechaUltimoPago),
                    YearMonth.from(fechaVigente)
            );
            int mesesConTarifaAnterior = mesesEntrePagoYVigente - 1;
            long mesesEntreVigenteYAhora = ChronoUnit.MONTHS.between(
                    YearMonth.from(fechaVigente),
                    YearMonth.from(LocalDate.now(defaultZoneId))
            );
            long mesesConTarifaActual = mesesEntreVigenteYAhora - 1;
            return tarifaAnterior
                    .multiply(BigDecimal.valueOf(mesesConTarifaAnterior))
                    .add(tarifaActual)
                    .multiply(BigDecimal.valueOf(mesesConTarifaActual))
                    .multiply(new BigDecimal("2"))
                    .abs()
                    .setScale(2, RoundingMode.HALF_DOWN);
        }
        long mesesEntrePagoYAhora = ChronoUnit.MONTHS.between(
                YearMonth.from(fechaUltimoPago),
                YearMonth.from(LocalDate.now(defaultZoneId))
        );
        long mesesConTarifaActual = mesesEntrePagoYAhora - 1;
        return tarifaActual
                .multiply(BigDecimal.valueOf(mesesConTarifaActual))
                .abs()
                .setScale(2, RoundingMode.HALF_DOWN);
    }

    @FunctionalInterface
    public interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }
}
