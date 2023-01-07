package com.revok.pagoEnLineaApi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@NamedNativeQuery(name = "findAllHistoricoAuxiliar",
        query = "SELECT concepto, importe_a, importe_v" +
                ", (SELECT descripcion FROM ConceptosPago WHERE cvconcepto = h.concepto) AS nombre" +
                " FROM historicoAuxiliar h" +
                " WHERE cvcontrato = ? ",
        resultSetMapping = "findAllHistoricoAuxiliarMapping"
)
@SqlResultSetMapping(name = "findAllHistoricoAuxiliarMapping",
        classes = @ConstructorResult(
                targetClass = HistoricoAuxiliar.class,
                columns = {
                        @ColumnResult(name = "concepto", type = Integer.class),
                        @ColumnResult(name = "importe_a"),
                        @ColumnResult(name = "importe_v"),
                        @ColumnResult(name = "nombre"),
                }
        ))
public class HistoricoAuxiliar {
    @Id
    private Integer concepto;
    private String importe_a;
    private String importe_v;
    private String nombre;
}
