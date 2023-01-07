package com.revok.pagoEnLineaApi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@NamedNativeQuery(name = "findUltimaLectura",
        query = " SELECT TOP 1 lecactual AS lecturaActual, lecanterior AS lecturaAnterior, consumo" +
                " FROM lecturas" +
                " WHERE cvcontrato = ?" +
                " ORDER BY fcodlectura DESC",
        resultSetMapping = "findUltimaLecturaMapping"
)
@SqlResultSetMapping(name = "findUltimaLecturaMapping",
        classes = @ConstructorResult(
                targetClass = UltimaLectura.class,
                columns = {
                        @ColumnResult(name = "lecturaActual", type = Integer.class),
                        @ColumnResult(name = "lecturaAnterior", type = Integer.class),
                        @ColumnResult(name = "consumo", type = Integer.class),
                }
        ))
public class UltimaLectura {


    private Integer lecturaActual;
    private Integer lecturaAnterior;
    @Id
    private Integer consumo;
}
