package com.revok.pagoEnLineaApi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@NamedNativeQuery(name = "findFactura",
        query = "SELECT d.razonSocial, d.rfc, d.codpos AS codigoPostal, d.estado, d.municipio," +
                " d.localidad, d.colonia, d.calle, d.numint, d.tipoPersona, d.UsoCDFI AS usoCfdi, u.nombre AS cfdiNombre" +
                " FROM dat_facturas d JOIN Cat_UsoCDFISat u ON d.UsoCDFI = u.clavesat" +
                " WHERE d.cvcontrato = ?",
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
                        @ColumnResult(name = "usoCfdi"),
                        @ColumnResult(name = "cfdiNombre"),

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
    private String usoCfdi;
    private String cfdiNombre;
}
