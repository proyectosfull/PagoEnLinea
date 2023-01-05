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
                targetClass = Propietario.class,
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
public class Factura {
    private String razonSocial;
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
