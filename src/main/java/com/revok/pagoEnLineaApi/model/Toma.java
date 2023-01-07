package com.revok.pagoEnLineaApi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@NamedNativeQuery(name = "findToma",
        query = "SELECT t.cvcliente, t.calle, t.numint" +
                ", CASE WHEN conmedidor = 'SI' THEN 1 ELSE 0 END AS tieneMedidor" +
                ", t.saneamiento, t.numfamilia" +
                ", t.codpost AS codigoPostal, CASE WHEN t.apmanten = 'SI' THEN 1 ELSE 0 END AS especial" +
                ", g.cvgiros, g.nombre as nombreGiro, g.tanterior AS tarifaAnterior, g.tarifactual AS tarifaActual, g.saneamiento AS saneamientoGiro" +
                ", g.totalconsane AS TarifaConSaneamiento, g.descinap as descuentoInap" +
                ", estados.nombre AS nombreEstado" +
                ", municipios.nombre AS nombreMunicipio" +
                ", colonia.cvcolonia, colonia.nombre AS nombreColonia" +
                ", localidad.nombre AS nombreLocalidad" +
                ", status.nombre as nombreEstatus" +
                ", t.observa" +
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
                        @ColumnResult(name = "saneamiento"),
                        @ColumnResult(name = "numfamilia", type = Integer.class),
                        @ColumnResult(name = "codigoPostal"),
                        @ColumnResult(name = "especial", type = Boolean.class),
                        @ColumnResult(name = "cvgiros", type = Integer.class),
                        @ColumnResult(name = "nombreGiro"),
                        @ColumnResult(name = "tarifaAnterior", type = Float.class),
                        @ColumnResult(name = "tarifaActual", type = Float.class),
                        @ColumnResult(name = "saneamientoGiro", type = Float.class),
                        @ColumnResult(name = "TarifaConSaneamiento", type = Float.class),
                        @ColumnResult(name = "descuentoInap"),
                        @ColumnResult(name = "nombreEstado"),
                        @ColumnResult(name = "nombreMunicipio"),
                        @ColumnResult(name = "nombreColonia"),
                        @ColumnResult(name = "nombreLocalidad"),
                        @ColumnResult(name = "nombreEstatus"),
                        @ColumnResult(name = "observa"),
                }
        ))
public class Toma {
    @Id
    private String cvcliente;
    private String calle;
    private String numint;
    private Boolean tieneMedidor;
    private String saneamiento;
    private Integer numfamilia;
    private String codigoPostal;
    private Boolean especial;
    private Integer cvgiros;
    private String nombreGiro;
    private Float tarifaAnterior;
    private Float tarifaActual;
    private Float saneamientoGiro;
    private Float TarifaConSaneamiento;
    private String descuentoInap;
    private String nombreEstado;
    private String nombreMunicipio;
    private String nombreColonia;
    private String nombreLocalidad;
    private String nombreEstatus;
    private String observa;
}
