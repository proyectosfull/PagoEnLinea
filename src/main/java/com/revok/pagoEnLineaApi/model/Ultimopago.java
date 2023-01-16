package com.revok.pagoEnLineaApi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@NamedNativeQuery(name = "findUltimoPago",
        query = "SELECT TOP 1 numrecibo AS numeroRecibo, total, fechareg AS fechaRegistro" +
                " FROM PagoGlobal" +
                " WHERE cvcontrato = ? AND status ='PAGADO'" +
                " ORDER BY fcodreg DESC",
        resultSetMapping = "findUltimoPagoMapping"
)
@SqlResultSetMapping(name = "findUltimoPagoMapping",
        classes = @ConstructorResult(
                targetClass = Ultimopago.class,
                columns = {
                        @ColumnResult(name = "numeroRecibo", type = Integer.class),
                        @ColumnResult(name = "total", type = Float.class),
                        @ColumnResult(name = "fechaRegistro"),
                }
        ))
public class Ultimopago {
    @Id
    private Integer numeroRecibo;
    private Float total;
    private String fechaRegistro;
    private String fechaCubre;
}
