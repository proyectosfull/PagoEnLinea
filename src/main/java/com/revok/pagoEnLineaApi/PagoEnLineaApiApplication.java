package com.revok.pagoEnLineaApi;

import jakarta.persistence.EntityManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class PagoEnLineaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PagoEnLineaApiApplication.class, args);
    }

    @Bean
    public CommandLineRunner initApp(RestTemplate restTemplate, EntityManager entityManager) {
        return args -> {
//            Query query = entityManager.createNamedQuery("findPropietario");
//            query.setParameter(1, "8002");
//            List<Propietario> propietarios = (List<Propietario>) query.getResultList();
//            System.out.println("end query");
        };
    }
}
