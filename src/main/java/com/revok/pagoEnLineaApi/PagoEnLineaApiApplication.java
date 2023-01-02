package com.revok.pagoEnLineaApi;

import com.revok.pagoEnLineaApi.model.Estado;
import com.revok.pagoEnLineaApi.repository.EstadoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;

@SpringBootApplication
public class PagoEnLineaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PagoEnLineaApiApplication.class, args);
    }

    @Bean
    public CommandLineRunner initApp(RestTemplate restTemplate, EstadoRepository estadoRepository) {
        return args -> {
            List<Estado> estados = estadoRepository.findAll();
            for (Estado e:estados) {
                System.out.println("id: " + e.getId() + " ,nombre: " + e.getNombre());
            }
//            String url = "https://api.weatherapi.com/v1/forecast.json?key={key}&q={q}&days={days}&aqi=no&alerts=no";
//            HashMap<String, String> params = new HashMap<>();
//            params.put("key", "fb8e28fe39cb4ab19c6183631222912");
//            params.put("q", "Temixco, Mor");
//            params.put("days", "1");
//            Thread thread = new Thread(() -> {
//                ResponseEntity<Whether> whetherResponse = restTemplate.getForEntity(url, Whether.class, params);
//                System.out.println("whether response code: " + whetherResponse.getStatusCode());
//                if (whetherResponse.hasBody()) {
//                    assert whetherResponse.getBody() != null;
//                    System.out.println("location name: " + whetherResponse.getBody().getLocation().getName());
//                    System.out.println("location region: " + whetherResponse.getBody().getLocation().getRegion());
//                    System.out.println("location country: " + whetherResponse.getBody().getLocation().getCountry());
//                }
//            });
//            thread.start();
        };
    }
}
