package com.revok.pagoEnLineaApi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@NamedNativeQuery(name = "findConvenio",
        query = "SELECT no_conv AS numeroConvenio, adeudototal AS adeudoTotal, status AS estatus" +
                ", porpagar AS porPagar, enganche" +
                ", tipopago AS tipoPago, realizo" +
                " FROM convenios" +
                " WHERE (status <> 'PAGADO' AND status <> 'CANCELADO')" +
                " AND porpagar > 0 and cvcontrato = ?" +
                " ORDER BY fcodinicia DESC",
        resultSetMapping = "findConvenioMapping"
)
@SqlResultSetMapping(name = "findConvenioMapping",
        classes = @ConstructorResult(
                targetClass = Convenio.class,
                columns = {
                        @ColumnResult(name = "numeroConvenio", type = Integer.class),
                        @ColumnResult(name = "adeudoTotal", type = Float.class),
                        @ColumnResult(name = "estatus"),
                        @ColumnResult(name = "porPagar", type = Float.class),
                        @ColumnResult(name = "enganche", type = Float.class),
                        @ColumnResult(name = "tipoPago"),
                        @ColumnResult(name = "realizo"),

                }
        ))
public class Convenio {
    private Integer numeroConvenio;
    private Float adeudoTotal;
    private String estatus;
    private Float porPagar;
    private Float enganche;
    private String tipoPago;
    private String realizo;
}
