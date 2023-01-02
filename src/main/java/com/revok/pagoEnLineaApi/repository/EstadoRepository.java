package com.revok.pagoEnLineaApi.repository;

import com.revok.pagoEnLineaApi.model.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Short> {
}
