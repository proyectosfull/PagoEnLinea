package com.revok.pagoEnLineaApi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@NamedNativeQuery(name = "findAllParcialidadConcepto",
        query = " SELECT p.cvconcepto, c.descripcion, p.costo, p.fecha" +
                " FROM PagosConceptos p" +
                " LEFT JOIN ConceptosPago c ON p.cvconcepto = c.cvconcepto" +
                " WHERE c.Activo = 'SI' AND p.status = 'POR PAGAR'" +
                " AND p.Costo != 0 AND p.cvconcepto = '4' AND p.cvcontrato = ?" +
                " ORDER BY p.fecha ASC",
        resultSetMapping = "findAllParcialidadConceptoMapping"
)
@SqlResultSetMapping(name = "findAllParcialidadConceptoMapping",
        classes = @ConstructorResult(
                targetClass = ParcialidadConcepto.class,
                columns = {
                        @ColumnResult(name = "cvconcepto"),
                        @ColumnResult(name = "descripcion"),
                        @ColumnResult(name = "costo", type = Float.class),
                        @ColumnResult(name = "fecha"),
                }
        ))
public class ParcialidadConcepto {
    @Id
    private String cvconcepto;
    private String descripcion;
    private Float costo;
    private String fecha;
}
