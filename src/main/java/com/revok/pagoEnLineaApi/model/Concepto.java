package com.revok.pagoEnLineaApi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@NamedNativeQuery(name = "findConceptosPago",
        query = "SELECT c.cvconcepto, c.descripcion, SUM(p.Costo) as costo" +
                " FROM PagosConceptos p left join ConceptosPago c on p.cvconcepto=c.cvconcepto  where (status<>'PAGADO' and status<>'CANCELADO') and c.Activo='SI' and p.cvconcepto not in (" + excepciones + ") and p.Costo!=0 and cvcontrato='" + cvcontrato + "' group by c.cvconcepto, c.descripcion order by c.descripcion asc",
        resultSetMapping = "findConceptosPagoMapping"
)
@SqlResultSetMapping(name = "findConceptosPagoMapping",
        classes = @ConstructorResult(
                targetClass = Toma.class,
                columns = {
                        @ColumnResult(name = "cvcliente"),

                }
        ))
public class Concepto {
}
