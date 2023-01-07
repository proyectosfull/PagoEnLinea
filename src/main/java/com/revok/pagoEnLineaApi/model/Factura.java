package com.revok.pagoEnLineaApi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@NamedNativeQuery(name = "findFactura",
        query = "SELECT razonSocial, rfc, codpos AS codigoPostal, estado, municipio, localidad, colonia, calle" +
                ",numint, tipoPersona" +
                " FROM dat_facturas" +
                " WHERE cvcontrato = ?",
        resultSetMapping = "findFacturaMapping"
)
@SqlResultSetMapping(name = "findFacturaMapping",
        classes = @ConstructorResult(
                targetClass = Factura.class,
                columns = {
                        @ColumnResult(name = "razonSocial"),
                        @ColumnResult(name = "rfc"),
                        @ColumnResult(name = "codigoPostal"),
                        @ColumnResult(name = "estado"),
                        @ColumnResult(name = "municipio"),
                        @ColumnResult(name = "localidad"),
                        @ColumnResult(name = "colonia"),
                        @ColumnResult(name = "calle"),
                        @ColumnResult(name = "numint"),
                        @ColumnResult(name = "tipoPersona"),

                }
        ))
public class Factura {
    private String razonSocial;
    @Id
    private String rfc;
    private String codigoPostal;
    private String estado;
    private String municipio;
    private String localidad;
    private String colonia;
    private String calle;
    private String numint;
    private String tipoPersona;
}
