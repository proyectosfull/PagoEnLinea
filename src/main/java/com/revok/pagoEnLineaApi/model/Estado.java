package com.revok.pagoEnLineaApi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "Estados")
@Data
public class Estado {
    @Id
    @Column(name = "cvestado")
    Short id;
    String nombre;
}
