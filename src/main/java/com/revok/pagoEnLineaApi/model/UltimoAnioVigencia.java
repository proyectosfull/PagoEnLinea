package com.revok.pagoEnLineaApi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@NamedNativeQuery(name = "findUltimoAnioVigencia",
        query = "SELECT MAX(YEAR(fechavigencia)) AS maxVigenciaTarifa" +
                ", MIN(YEAR(fechavigencia)) AS minVigenciaTarifa" +
                ", MAX(YEAR(fsanevigencia)) AS maxVigenciaSaneamiento" +
                ", MIN(YEAR(fsanevigencia)) AS minVigenciaSaneamiento" +
                " FROM Tarifas WHERE cvgiro = ?",
        resultSetMapping = "findUltimoAnioVigenciaMapping"
)
@SqlResultSetMapping(name = "findUltimoAnioVigenciaMapping",
        classes = @ConstructorResult(
                targetClass = UltimoAnioVigencia.class,
                columns = {
                        @ColumnResult(name = "maxVigenciaTarifa", type = Integer.class),
                        @ColumnResult(name = "minVigenciaTarifa", type = Integer.class),
                        @ColumnResult(name = "maxVigenciaSaneamiento", type = Integer.class),
                        @ColumnResult(name = "minVigenciaSaneamiento", type = Integer.class),
                }
        ))
public class UltimoAnioVigencia {
    @Id
    Integer maxVigenciaTarifa = 0;
    Integer minVigenciaTarifa = 0;
    Integer maxVigenciaSaneamiento = 0;
    Integer minVigenciaSaneamiento = 0;
}
