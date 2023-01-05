package com.revok.pagoEnLineaApi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@NamedNativeQuery(name = "findUltimoPago",
        query = "SELECT TOP 1 numrecibo AS numeroRecibo, total, fechareg AS fechaRegistro" +
                " FROM PagoGlobal" +
                " WHERE cvcontrato = ? AND status ='PAGADO'",
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
@NamedNativeQuery(name = "findFechaCubreFromLecturas",
        query = "SELECT TOP 1 feclectura" +
                " FROM lecturas" +
                " WHERE cvcontrato = ? AND cvstatus='PAGADO'" +
                " ORDER BY fcodlectura desc",
        resultClass = String.class)
@NamedNativeQuery(name = "findFechaCubreFromControlTomas",
        query = "SELECT TOP 1 fechacubre" +
                " FROM ControlTomas" +
                " WHERE cvcontrato = ?",
        resultClass = String.class)
public class Ultimopago {
    private Integer numeroRecibo;
    private Float total;
    private String fechaRegistro;
    private String fechaCubre;
}
