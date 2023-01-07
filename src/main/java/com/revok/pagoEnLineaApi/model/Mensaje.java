package com.revok.pagoEnLineaApi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@NamedNativeQuery(name = "findMensaje",
        query = " SELECT TOP 1 mensaje ,fecha" +
                " FROM MensajesRecibo" +
                " WHERE cvcontrato = ? AND entregado = 'NO'" +
                " ORDER BY fcodreg DESC",
        resultSetMapping = "findMensajeMapping"
)
@SqlResultSetMapping(name = "findMensajeMapping",
        classes = @ConstructorResult(
                targetClass = Mensaje.class,
                columns = {
                        @ColumnResult(name = "mensaje"),
                        @ColumnResult(name = "fecha"),
                }
        ))
public class Mensaje {
    @Id
    private String mensaje;
    private String fecha;
}
