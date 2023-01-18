package com.revok.pagoEnLineaApi.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@NamedNativeQuery(name = "findToma",
        query = "SELECT t.cvcliente, t.calle, t.numint" +
                ", CASE WHEN conmedidor = 'SI' THEN 1 ELSE 0 END AS tieneMedidor" +
                ", CASE WHEN t.saneamiento = 'SI' THEN 1 ELSE 0 END AS saneamiento, t.numfamilia" +
                ", t.codpost AS codigoPostal, CASE WHEN t.apmanten = 'SI' THEN 1 ELSE 0 END AS especial" +
                ", g.cvgiros as cvgiro, g.nombre AS nombreGiro, g.tanterior AS tarifaAnterior" +
                ", g.tarifactual AS tarifaActual, g.saneamiento AS saneamientoGiro" +
                ", g.totalconsane AS TarifaConSaneamiento" +
                ", CASE WHEN g.descinap = 'SI' THEN 1 ELSE 0 END AS descuentoInap" +
                ", estados.nombre AS nombreEstado" +
                ", municipios.nombre AS nombreMunicipio" +
                ", colonia.cvcolonia, colonia.nombre AS nombreColonia" +
                ", localidad.nombre AS nombreLocalidad" +
                ", status.nombre AS nombreEstatus" +
                ", t.observa, g.fecvigencia AS fechaVigenciaTarifaGiro" +
                ", g.fechasaneamiento AS fechaVigenciaSaneamientoGiro" +
                " FROM dattomas t" +
                " LEFT JOIN giros g ON g.cvgiros = t.uso" +
                " LEFT JOIN status ON status.cvstatus = t.statustom" +
                " LEFT JOIN estados ON estados.cvestado = t.cvestado" +
                " LEFT JOIN municipios ON municipios.cvmunicipio = t.cvmunicipio" +
                " LEFT JOIN colonia ON colonia.cvcolonia = t.cvcolonia" +
                " LEFT JOIN localidad ON localidad.cvlocalidad = t.cvlocalidad" +
                " WHERE t.cvcontrato = ?",
        resultSetMapping = "findTomaMapping"
)
@SqlResultSetMapping(name = "findTomaMapping",
        classes = @ConstructorResult(
                targetClass = Toma.class,
                columns = {
                        @ColumnResult(name = "cvcliente"),
                        @ColumnResult(name = "calle"),
                        @ColumnResult(name = "numint"),
                        @ColumnResult(name = "tieneMedidor", type = Boolean.class),
                        @ColumnResult(name = "saneamiento", type = Boolean.class),
                        @ColumnResult(name = "numfamilia", type = Integer.class),
                        @ColumnResult(name = "codigoPostal"),
                        @ColumnResult(name = "especial", type = Boolean.class),
                        @ColumnResult(name = "cvgiro", type = Integer.class),
                        @ColumnResult(name = "nombreGiro"),
                        @ColumnResult(name = "tarifaAnterior", type = BigDecimal.class),
                        @ColumnResult(name = "tarifaActual", type = BigDecimal.class),
                        @ColumnResult(name = "saneamientoGiro", type = BigDecimal.class),
                        @ColumnResult(name = "TarifaConSaneamiento", type = BigDecimal.class),
                        @ColumnResult(name = "descuentoInap", type = Boolean.class),
                        @ColumnResult(name = "nombreEstado"),
                        @ColumnResult(name = "nombreMunicipio"),
                        @ColumnResult(name = "nombreColonia"),
                        @ColumnResult(name = "nombreLocalidad"),
                        @ColumnResult(name = "nombreEstatus"),
                        @ColumnResult(name = "observa"),
                        @ColumnResult(name = "fechaVigenciaTarifaGiro"),
                        @ColumnResult(name = "fechaVigenciaSaneamientoGiro"),
                }
        ))
public class Toma {
    @Id
    private String cvcliente;
    private String calle;
    private String numint;
    private Boolean tieneMedidor;
    private Boolean saneamiento;
    private Integer numfamilia;
    private String codigoPostal;
    private Boolean especial;
    private Integer cvgiro;
    private String nombreGiro;
    private BigDecimal tarifaAnterior = BigDecimal.ZERO;
    private BigDecimal tarifaActual = BigDecimal.ZERO;
    private BigDecimal saneamientoGiro = BigDecimal.ZERO;
    private BigDecimal TarifaConSaneamiento = BigDecimal.ZERO;
    private Boolean descuentoInap;
    private String nombreEstado;
    private String nombreMunicipio;
    private String nombreColonia;
    private String nombreLocalidad;
    private String nombreEstatus;
    private String observa;
    private String fechaVigenciaTarifaGiro;
    private String fechaVigenciaSaneamientoGiro;
}
