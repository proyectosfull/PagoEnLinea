package com.revok.pagoEnLineaApi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@NamedNativeQuery(name = "findReferenciaBancaria",
        query = "SELECT TOP 1 CONCAT(digitosLibres, digitosFecha, digitosImporte, digitosReferencia) AS referencia" +
                " FROM HistorialReferenciasCIE" +
                " WHERE cvcontrato = ?" +
                " AND CONVERT(date, fechaVigencia) >= ?",
        resultSetMapping = "findReferenciaBancariaMapping"
)
@SqlResultSetMapping(name = "findReferenciaBancariaMapping",
        classes = @ConstructorResult(
                targetClass = ReferenciaBancaria.class,
                columns = {
                        @ColumnResult(name = "referencia"),
                }
        ))
public class ReferenciaBancaria {
    /** "" default value */
    @Id
    private String referencia = "";
}
