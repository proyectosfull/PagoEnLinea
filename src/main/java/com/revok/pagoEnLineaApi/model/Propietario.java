package com.revok.pagoEnLineaApi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;


@Data
@Entity
@NamedNativeQuery(name = "findPropietario",
        query = "SELECT d.cvcliente" +
                ",CONCAT(d.nombre, ' ', d.appaterno, ' ', d.apmaterno) AS nombreCompleto" +
                ",d.nombre, d.appaterno, d.apmaterno" +
                ",d.calle, d.numint, d.coninsen, d.rfc, d.email, d.tel,d.celular" +
                " FROM datclientes d INNER JOIN dattomas t ON t.cvcliente=d.cvcliente" +
                " WHERE t.cvcontrato = ?",
        resultSetMapping = "findPropietarioMapping"
)
@NamedNativeQuery(name = "findPropietarioByFullname",
        query = "SELECT d.cvcliente" +
                ",CONCAT(d.nombre, ' ', d.appaterno, ' ', d.apmaterno) AS nombreCompleto" +
                ",d.nombre, d.appaterno, d.apmaterno" +
                ",d.calle, d.numint, d.coninsen, d.rfc, d.email, d.tel,d.celular" +
                " FROM datclientes d INNER JOIN dattomas t ON t.cvcliente=d.cvcliente" +
                " WHERE CONCAT(d.nombre, d.appaterno, d.apmaterno) = ?",
        resultSetMapping = "findPropietarioMapping"
)
@SqlResultSetMapping(name = "findPropietarioMapping",
        classes = @ConstructorResult(
                targetClass = Propietario.class,
                columns = {
                        @ColumnResult(name = "calle"),
                        @ColumnResult(name = "numint"),
                        @ColumnResult(name = "coninsen"),
                        @ColumnResult(name = "rfc"),
                        @ColumnResult(name = "email"),
                        @ColumnResult(name = "tel"),
                        @ColumnResult(name = "celular"),
                        @ColumnResult(name = "cvcliente"),
                        @ColumnResult(name = "nombre"),
                        @ColumnResult(name = "appaterno"),
                        @ColumnResult(name = "apmaterno"),
                        @ColumnResult(name = "nombreCompleto"),
                }
        ))
public class Propietario implements UserDetails {
    @Id
    private String cvcliente;
    private String calle;
    private String numint;
    private String coninsen;
    private String rfc;
    private String email;
    private String tel;
    private String celular;
    private String nombre;
    private String appaterno;
    private String apmaterno;
    private String nombreCompleto;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return getNombre() + getAppaterno() + getApmaterno();
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
