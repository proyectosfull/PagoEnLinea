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

    @SuppressWarnings("unchecked")
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
        return !query.getResultList().isEmpty();
    }

    @SuppressWarnings("unchecked")
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
        contrato.setCvcontrato(cvcontrato);
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

    @SuppressWarnings("unchecked")
    public List<Parametro> findAllParametro() {
        return entityManager.createNamedQuery("findAllParametro").getResultList();
    }

    public Deuda findCuentaPorPagarFromMonths(Contrato contrato, int meses, Departamento departamento) {
        meses = meses > 0 ? meses : contrato.getMesesPorPagar();

        List<Parametro> parametros = findAllParametro();
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

        float totalCuota = 0;
        float totalSaneamiento = 0;
        float totalGastosCobranza = 0;
        float totalRecargos = 0;

        float tarifa = 0f;
        float saneamiento = 0;

        if (departamento == Departamento.CENTRO) {
            Query query = entityManager.createNamedQuery("findUltimoAnioVigencia");
            query.setParameter(1, contrato.getToma().getCvgiro());
            UltimoAnioVigencia ultimoAnioVigencia = (UltimoAnioVigencia) query.getSingleResult();
            for (int i = 1; i <= meses; i++) {
                // Obtener el valor de la tarifa del historial de acuerdo a la fecha.
                Query queryTarifa = entityManager.createNativeQuery(" SELECT TOP 1 tagua_actual" +
                        " FROM tarifas WHERE cvgiro = ? AND YEAR(fechavigencia) = ?" +
                        " ORDER BY CONVERT(date, fechavigencia) DESC");
                queryTarifa.setParameter(1, contrato.getToma().getCvgiro());
                queryTarifa.setParameter(2,
                        (fechaUltimoPago.getYear() < ultimoAnioVigencia.getMinVigenciaTarifa() ?
                                ultimoAnioVigencia.getMinVigenciaTarifa() :
                                (fechaUltimoPago.getYear() > ultimoAnioVigencia.getMaxVigenciaTarifa() ?
                                        ultimoAnioVigencia.getMaxVigenciaTarifa() : fechaUltimoPago.getYear()))
                );
                List<?> queryTarifaResult = queryTarifa.getResultList();
                totalCuota += !queryTarifaResult.isEmpty() ? (double) queryTarifaResult.get(0) : 0f;

                Query querySaneamiento = entityManager.createNativeQuery(" SELECT TOP 1 tsane_actual" +
                        " FROM tarifas WHERE cvgiro = ?" +
                        " AND YEAR(fsanevigencia) = ?"
                        + " ORDER BY CONVERT(date, fsanevigencia) DESC");
                querySaneamiento.setParameter(1, contrato.getToma().getCvgiro());
                querySaneamiento.setParameter(2,
                        (fechaUltimoPago.getYear() < ultimoAnioVigencia.getMinVigenciaSaneamiento() ?
                                ultimoAnioVigencia.getMinVigenciaSaneamiento() :
                                (fechaUltimoPago.getYear() > ultimoAnioVigencia.getMaxVigenciaSaneamiento() ?
                                        ultimoAnioVigencia.getMaxVigenciaSaneamiento() : fechaUltimoPago.getYear()))
                );

                List<?> querySaneamientoResult = querySaneamiento.getResultList();
                totalSaneamiento += querySaneamientoResult.size() > 0f ? (double) querySaneamientoResult.get(0) : 0f;
                // Si el contador es  menor a la cantidad de meses total, se agregan recargos
                // (cuando sea igual a los meses de deuda se asimila que es el mes corriente y no se agrega recargo).
                if (i < mesesDeuda && !contrato.getToma().getEspecial() && totalRecargos < maxRecargos && parametroRecargo != null) {
                    totalRecargos += (tarifa + saneamiento) * parametroRecargo.getCantidad();
                }
            }
        } else {
            for (int i = 0; i < meses; i++) {
                // Obtener el valor de la tarifa actual del giro.
                tarifa = giroTarifaActual;
                totalCuota = totalCuota + tarifa;

                // Obtener el valor del saneamiento actual del giro.
                saneamiento = giroSaneamientoActual;
                totalSaneamiento += saneamiento;

                if (i < mesesDeuda && !contrato.getToma().getEspecial() && totalRecargos < maxRecargos && parametroRecargo != null) {
                    totalRecargos += (tarifa + saneamiento) * parametroRecargo.getCantidad();
                }
            }
        }

        // Si los meses vencidos son mayor o igual al de máximo de gastos de cobranza
        // se agregan a la deuda.
        if (parametroMesesGastosCobransa != null && mesesConRecargos < parametroMesesGastosCobransa.getCantidad()) {
            // Obtener meses con gastos de cobranza restando a los meses de adeudo la cantidad
            // de meses en la que se comienza a cobrar esos gastos de cobranza.
            int mesesConGastosCobranza = mesesDeuda - parametroMesesGastosCobransa.getCantidad().intValue();
            // Obtener los meses a pagar que no incluyen gastos de cobranza.
            int mesesPagarSinGastosCobranza = mesesDeuda - mesesConGastosCobranza;
            // Si paga más de con gastos de cobranza, se quitan l
            float tarifaConGastosCobranza = (mesesDeuda <= mesesConGastosCobranza) ? totalCuota : (totalCuota - (tarifa * mesesPagarSinGastosCobranza));
            float saneamientoConGastosCobranza = (mesesDeuda <= mesesConGastosCobranza) ? totalSaneamiento : (totalSaneamiento - (saneamiento * mesesPagarSinGastosCobranza));

            if (parametroGastosCobransa != null && !contrato.getToma().getEspecial()) {
                totalGastosCobranza = (tarifaConGastosCobranza + saneamientoConGastosCobranza + totalRecargos) * parametroGastosCobransa.getCantidad();
            }
        }

        Deuda deuda = new Deuda();

        // verificar si descuento de 12x11 esta activo; aplicarlo en caso de estar activo
        Concepto conceptoParticular;

        // buscar concepto de  descuento por campaña promocional anual en caso de estar activo.
        // valor nulo en caso contrario
        conceptoParticular = findConceptoDescuento12x11(contrato, deuda);
        if (conceptoParticular != null) {
            deuda.getConceptos().add(conceptoParticular);
            deuda.setTotalPagar(deuda.getTotalPagar() + conceptoParticular.getCosto());
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

            if (deuda.getTotalRecargos() > 0) {
                conceptoParticular = new Concepto();
                conceptoParticular.setCvconcepto(17);
                conceptoParticular.setDescripcion("RECARGOS");
                conceptoParticular.setCosto(deuda.getTotalRecargos());
                deuda.getConceptos().add(conceptoParticular);

                conceptoParticular = findConceptoDescuentoRecargos(deuda.getTotalRecargos(), departamento);
                if (conceptoParticular != null) {
                    deuda.getConceptos().add(conceptoParticular);
                    deuda.setTotalPagar(deuda.getTotalPagar() + conceptoParticular.getCosto());
                }
            }

            if (deuda.getTotalGastosCobranza() > 0) {
                conceptoParticular = new Concepto();
                conceptoParticular.setCvconcepto(8);
                conceptoParticular.setDescripcion("GASTOS DE COBRANZA");
                conceptoParticular.setCosto(deuda.getTotalGastosCobranza());
                deuda.getConceptos().add(conceptoParticular);

                conceptoParticular = findConceptoDescuentoGastosCobranza(deuda.getTotalRecargos(), departamento);
                if (conceptoParticular != null) {
                    deuda.getConceptos().add(conceptoParticular);
                    deuda.setTotalPagar(deuda.getTotalPagar() + conceptoParticular.getCosto());
                }
            }
        }


        deuda.setTarifaMensual(giroTarifaActual);
        deuda.setSaneamientoMensual(giroSaneamientoActual);
        deuda.setTotalGastosCobranza(totalGastosCobranza);
        deuda.setTotalRecargos(totalRecargos);
        deuda.setTotalSaneamiento(totalSaneamiento);
        deuda.setTotalCuotaOConsumo(totalCuota);
        deuda.setTotalPagar(totalCuota + totalSaneamiento + totalRecargos + totalGastosCobranza);
        deuda.setFechaCubre(fechaUltimoPago);
        deuda.setFechaUltimoPago(fechaUltimoPago);
        return deuda;
    }

    private Concepto findConceptoDescuento12x11(Contrato contrato, Deuda deuda) {
        boolean promotionActiva;
        String fechaFinPromocion;
        Query queryDescuento12x11 = entityManager.createNativeQuery("SELECT activapromocion, fehcadetiene FROM datosagua");
        @SuppressWarnings("unchecked")
        List<Object[]> resultQueryDescuento12x11 = (List<Object[]>) queryDescuento12x11.getResultList();
        if (resultQueryDescuento12x11.isEmpty()) {
            return null;
        }
        promotionActiva = (boolean) resultQueryDescuento12x11.get(0)[0];
        fechaFinPromocion = (String) resultQueryDescuento12x11.get(0)[1];

        if (!promotionActiva) {
            return null;
        }
        boolean promocionActivaParaGiro = false;
        Query queryAplicaDiciembre = entityManager.createNativeQuery("SELECT aplicadiciembre" +
                " FROM giros WHERE cvgiros = ?");
        queryAplicaDiciembre.setParameter(1, contrato.getToma().getCvgiro());
        @SuppressWarnings("unchecked")
        List<Object[]> resultQueryAplicaDiciembre = (List<Object[]>) queryAplicaDiciembre.getResultList();
        if (!resultQueryAplicaDiciembre.isEmpty()) {
            promocionActivaParaGiro = (boolean) resultQueryAplicaDiciembre.get(0)[0];
        }

        if (!promocionActivaParaGiro) {
            return null;
        }
        boolean esAreaComun = false;
        Query queryEsAreaComun = entityManager.createNativeQuery(" SELECT  COUNT(*) AS counter" +
                " FROM DatClientes c JOIN DatTomas t ON c.cvcliente = t.cvcliente" +
                " WHERE t.cvcontrato = ?" +
                " AND CONCAT(nombre, ' ', appaterno, ' ', apmaterno) LIKE '%AREA COMUN%'");
        queryEsAreaComun.setParameter(1, contrato.getCvcontrato());
        List<?> resultQueryEsAreaComun = queryEsAreaComun.getResultList();
        if (!resultQueryEsAreaComun.isEmpty()) {
            esAreaComun = (boolean) resultQueryEsAreaComun.get(0);
        }
        //no aplicable a contrato perteneciente a AREA COMUN
        if (esAreaComun) {
            return null;
        }
        LocalDate fechaFinPromocionTyped = LocalDate.parse(fechaFinPromocion, formatterReturn);
        fechaFinPromocionTyped = LocalDate.of(fechaFinPromocionTyped.getYear(), 12, 31);

        if (deuda.getFechaCubre().getMonth() != fechaFinPromocionTyped.getMonth() ||
                deuda.getFechaCubre().getYear() != fechaFinPromocionTyped.getYear()) {
            return null;
        }
        String nombrePromocion = "";
        // obtener descripcion del conceto DESCUENTO ANUAL
        Query queryDescuentoAnual = entityManager.createNativeQuery("SELECT descripcion" +
                " FROM conceptos WHERE cvconcepto='70'");
        List<?> resultQueryDescuentoAnual = queryDescuentoAnual.getResultList();
        if (!resultQueryDescuentoAnual.isEmpty()) {
            nombrePromocion = (String) resultQueryDescuentoAnual.get(0);
        }
        // Concepto de descuento 12x11
        Concepto concepto = new Concepto();
        concepto.setCvconcepto(70);
        concepto.setDescripcion(nombrePromocion);
        concepto.setCosto((deuda.getTarifaMensual()) + deuda.getSaneamientoMensual() * -1);
        return concepto;
    }


    private Concepto findConceptoDescuentoRecargos(float totalRecargos, Departamento departamento) {
        if (departamento != Departamento.CENTRO)
            return null;
        Query queryIsPromotionActive = entityManager.createNativeQuery("SELECT TOP 1" +
                " CASE WHEN promtodo = 'SI' THEN 1 ELSE 0 " +
                " FROM Datosagua", Boolean.class);
        List<?> resultIsPromotionActive = queryIsPromotionActive.getResultList();
        boolean isPromotionActive = !resultIsPromotionActive.isEmpty() && (boolean) resultIsPromotionActive.get(0);
        if (!isPromotionActive)
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
                " CASE WHEN promtodo = 'SI' THEN 1 ELSE 0 " +
                " FROM Datosagua", Boolean.class);
        List<?> resultIsPromotionActive = queryIsPromotionActive.getResultList();
        boolean isPromotionActive = !resultIsPromotionActive.isEmpty() && (boolean) resultIsPromotionActive.get(0);
        if (!isPromotionActive)
            return null;
        Concepto conceptoPromotion = new Concepto();
        conceptoPromotion.setCvconcepto(92);
        conceptoPromotion.setDescripcion("DESC. 50% DE GASTOS DE COBRANZA");
        conceptoPromotion.setCosto((totalGastos * 0.5f) * -1);
        return conceptoPromotion;
    }

    private float calcularMaxRangos(LocalDate fechaUltimoPago, LocalDate fechaVigente, float tarifaAnterior, float tarifaActual) {
        int mesesConTarifaAnterior;
        int mesesConTarifaActual;
        float resultado;
        if (fechaUltimoPago.isBefore(fechaVigente)) {
            int mesesEntrePagoYVigente = (int) ChronoUnit.MONTHS.between(
                    YearMonth.from(fechaUltimoPago),
                    YearMonth.from(fechaVigente)
            );
            mesesConTarifaAnterior = mesesEntrePagoYVigente - 1;
            int mesesEntreVigenteYAhora = (int) ChronoUnit.MONTHS.between(
                    YearMonth.from(fechaVigente),
                    YearMonth.from(LocalDate.now())
            );
            mesesConTarifaActual = mesesEntreVigenteYAhora - 1;
            return ((mesesConTarifaAnterior * tarifaAnterior) + (mesesConTarifaActual * tarifaActual)) * 2;
        } else {
            int mesesEntrePagoYAhora = (int) ChronoUnit.MONTHS.between(
                    YearMonth.from(fechaUltimoPago),
                    YearMonth.from(LocalDate.now())
            );
            mesesConTarifaActual = mesesEntrePagoYAhora - 1;
            resultado = (mesesConTarifaActual * tarifaActual) * 2;
        }

        if (resultado < 0)
            resultado = resultado * -1;
        return resultado;
    }
}
