package com.revok.pagoEnLineaApi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@NamedNativeQuery(name = "findAllConcepto",
        query = " SELECT c.cvconcepto, c.descripcion, SUM(p.costo) AS costo" +
                " FROM PagosConceptos p" +
                " LEFT JOIN ConceptosPago c ON p.cvconcepto = c.cvconcepto" +
                " WHERE (status <> 'PAGADO' AND status <> 'CANCELADO')" +
                " AND c.activo = 'SI' AND p.cvconcepto NOT IN ( 3,4,5,8,17,19,24,80,100,268,254 )" +
                " AND p.costo != 0 AND cvcontrato = ?" +
                " GROUP BY c.cvconcepto, c.descripcion" +
                " ORDER BY c.descripcion ASC",
        resultSetMapping = "findAllConceptoMapping"
)
@NamedNativeQuery(name = "findSaldoAFavorConcepto",
        query = " SELECT (SELECT TOP 1 cvconcepto FROM conceptospago WHERE descripcion LIKE '%SALDO%') AS cvconcepto" +
                ", 'SALDO A FAVOR' AS descripcion, saldoafavor AS costo" +
                " FROM SaldoaFavor" +
                " WHERE cvcontrato = ?",
        resultSetMapping = "findAllConceptoMapping"
)
@SqlResultSetMapping(name = "findAllConceptoMapping",
        classes = @ConstructorResult(
                targetClass = Concepto.class,
                columns = {
                        @ColumnResult(name = "cvconcepto", type = Integer.class),
                        @ColumnResult(name = "descripcion"),
                        @ColumnResult(name = "costo", type = Float.class),

                }
        ))
public class Concepto {
    @Id
    private Integer cvconcepto;
    private String descripcion;
    private Float costo;
}
