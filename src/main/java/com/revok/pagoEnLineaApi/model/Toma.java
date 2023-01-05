package com.revok.pagoEnLineaApi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@NamedNativeQuery(name = "findToma",
        query = "SELECT t.cvcliente, t.calle, t.numint, t.conmedidor, t.saneamiento, t.numfamilia," +
                " t.ayotoma, t.codpost AS codigoPostal" +
                ", t.apmanten AS especial, t.idanterior AS idAnterior" +
                ", g.cvgiros, g.nombre as nombreGiro, g.tanterior, g.tarifactual, g.saneamiento" +
                ", g.totalconsale AS TarifaConSaneamiento, g.descinap as descInap" +
                ", estados.nombre AS nombreEstado" +
                ", municipios.nombre AS nombreMunicipio" +
                ", colonia.cvcolonia, colonia.nombre AS nombreColonia" +
                ", localidad.nombre AS nombreColonia" +
                ", status.nombre as nombreStatus" +
                " FROM dattomas t" +
                " LEFT JOIN giros g ON g.cvgiros = t.uso" +
                " LEFT JOIN status ON status.cvstatus = t.statustom" +
                " LEFT JOIN estados ON estados.cvestado = t.cvestado" +
                " LEFT JOIN municipios ON municipios.cvmunicipio = t.cvmunicipio" +
                " LEFT JOIN colonia ON colonia.cvcolonia = t.cvcolonia" +
                " LEFT JOIN localidad ON localidad.cvlocalidad = t.cvlocalidad" +
                " WHERE t.cvcontrato = ?",
        resultSetMapping = "findPropietarioMapping"
)
@SqlResultSetMapping(name = "findTomaMapping",
        classes = @ConstructorResult(
                targetClass = Toma.class,
                columns = {
                        @ColumnResult(name = "calle"),
                        @ColumnResult(name = "numint"),
                        @ColumnResult(name = "coninsen"),
                        @ColumnResult(name = "rfc"),
                        @ColumnResult(name = "email"),
                        @ColumnResult(name = "tel"),
                        @ColumnResult(name = "celular"),
                        @ColumnResult(name = "cvcliente"),
                        @ColumnResult(name = "nombre"),
                        @ColumnResult(name = "appaterno"),
                        @ColumnResult(name = "apmaterno"),
                        @ColumnResult(name = "nombreCompleto"),
                }
        ))
public class Toma {
    private String cvcliente;
    private String calle;
    private String numint;
    private String conmedidor;
    private String saneamiento;
    private String numfamilia;
    private String ayotoma;
    private String codigoPostal;
    private Boolean especial;
    private Integer idAnterior;

    // confirmar tipo de dato de este campo
    private String cvgiros;
}
