package com.revok.pagoEnLineaApi.service;

import com.revok.pagoEnLineaApi.model.Propietario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class PropietarioService implements UserDetailsService {
    private final EntityManager entityManager;

    @Override
    @Transactional
    public Propietario loadUserByUsername(String username) throws UsernameNotFoundException {
        Query query = entityManager.createNamedQuery("findPropietarioByFullname");
        query.setParameter(1, Arrays.stream(username.split(" ")).reduce("", String::concat));
        List<?> result = query.getResultList();
        if (result.isEmpty())
            throw new UsernameNotFoundException("Nombre no encontrado");
        Propietario propietario = (Propietario) query.getSingleResult();
        log.debug(propietario.getUsername());
        return propietario;
    }
}
