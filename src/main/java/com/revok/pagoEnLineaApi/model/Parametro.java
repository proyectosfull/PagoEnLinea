package com.revok.pagoEnLineaApi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@NamedNativeQuery(name = "findAllParametro",
        query = " SELECT * FROM parametros ",
        resultSetMapping = "findAllParametroMapping"
)
@SqlResultSetMapping(name = "findAllParametroMapping",
        classes = @ConstructorResult(
                targetClass = Parametro.class,
                columns = {
                        @ColumnResult(name = "cvparam"),
                        @ColumnResult(name = "nombre"),
                        @ColumnResult(name = "cantidad", type = Float.class),

                }
        ))
public class Parametro {
    @Id
    private String cvparam;
    private String nombre;
    private Float cantidad;

    public enum ParametroType {

        RECA,
        GE,
        MESESGE,
        MESESRECA,
        SALMINANT,
        SALMINVIG
    }
}
