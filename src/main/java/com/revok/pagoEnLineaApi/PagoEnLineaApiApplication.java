package com.revok.pagoEnLineaApi;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@SpringBootApplication
public class PagoEnLineaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PagoEnLineaApiApplication.class, args);
    }

    @Bean
    @Transactional
    public CommandLineRunner initApp(RestTemplate restTemplate, EntityManager entityManager, @Value("${revok.normalization}") Boolean normalization) {

        if (!normalization) {
            return args -> {
            };
        }

        ZoneId defaultZoneId = ZoneId.of("America/Mexico_City");
        Locale defaultLocale = new Locale("spa", "MX");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", defaultLocale);
        DateTimeFormatter formatterReturn = DateTimeFormatter.ofPattern("dd/MM/yyyy", defaultLocale);

        System.out.println("estarization begins");

        int count = 0;

        // normalización 1: aplicación de formato estándar de fecha (yyyy-mm-dd) sobre fecantes y feclectura en lecturas
        Query queryLecturas = entityManager.createNativeQuery("SELECT cvlectura, fecantes, feclectura FROM lecturas WHERE feclectura LIKE '__/__/____'");
        @SuppressWarnings("unchecked")
        List<Object[]> resultLecturas = queryLecturas.getResultList();
        for (Object[] record : resultLecturas) {
            Integer cvlectura = (Integer) record[0];
            LocalDate fechaAntes = LocalDate.parse((String) record[1], formatterReturn);
            LocalDate fechaLectura = LocalDate.parse((String) record[2], formatterReturn);
            Query queryUpdate = entityManager.createNativeQuery("UPDATE lecturas SET fecantes = ?, feclectura = ? WHERE cvlectura = ?");
            queryUpdate.setParameter(1, fechaAntes.format(formatter));
            queryUpdate.setParameter(2, fechaLectura.format(formatter));
            queryUpdate.setParameter(3, cvlectura);
            queryUpdate.executeUpdate();
            count++;
            System.out.println(count);
        }

        System.out.println("step 1 complete");

        // normalización 2: numfamilia < 1 => 1. aplicado a tabla dattomas
        Query queryNumFamilias = entityManager.createNativeQuery("SELECT cvcontrato FROM dattomas WHERE numfamilia < 1");
        @SuppressWarnings("unchecked")
        List<Integer> resultNumFamilia = queryNumFamilias.getResultList();
        for (Integer cvcontrato : resultNumFamilia) {
            Query queryUpdate = entityManager.createNativeQuery("UPDATE dattomas SET numfamilia = 1 WHERE cvcontrato = ?");
            queryUpdate.setParameter(1, cvcontrato);
            queryUpdate.executeUpdate();
            count++;
            System.out.println(count);
        }

        System.out.println("step 2 complete");

        // Normalización 3: formato de fecha en pagoGlobal.
        Query queryFechasPagoGlobal = entityManager.createNativeQuery(
                "SELECT cvcontrato, fechareg, fcubre, fechaantes, numrecibo" +
                        " FROM pagoglobal" +
                        " WHERE fechareg LIKE '__/__/____'" +
                        " AND fcubre LIKE '__/__/____'" +
                        " AND fechaantes LIKE '__/__/____'");
        @SuppressWarnings("unchecked")
        List<Object[]> resultFechasPagoGlobal = queryFechasPagoGlobal.getResultList();
        for (Object[] record : resultFechasPagoGlobal) {
            String cvcontrato = (String) record[0];
            String fechaRegistro = (String) record[1];
            LocalDate fechaRegistroTyped = LocalDate.parse(fechaRegistro, formatterReturn);
            LocalDate fechaCubre = LocalDate.parse((String) record[2], formatterReturn);
            LocalDate fechaAntes = LocalDate.parse((String) record[3], formatterReturn);
            Integer numeroRecibo = (Integer) record[4];
            Query queryUpdate = entityManager.createNativeQuery(
                    "UPDATE pagoglobal SET fechareg = ?, fcubre = ?, fechaantes = ? WHERE cvcontrato = ? AND fechareg = ? AND numrecibo = ?");
            queryUpdate.setParameter(1, fechaRegistroTyped.format(formatter));
            queryUpdate.setParameter(2, fechaCubre.format(formatter));
            queryUpdate.setParameter(3, fechaAntes.format(formatter));
            queryUpdate.setParameter(4, cvcontrato);
            queryUpdate.setParameter(5, fechaRegistro);
            queryUpdate.setParameter(6, numeroRecibo);
            queryUpdate.executeUpdate();
            count++;
            System.out.println(count);
        }

        System.out.println("step 3 complete");

        // normalización 4: formato de fecha en fechacubre y freinstala para controltomas
        //1. insert default value to second argument
        Query queryFechaReinstalacionDefault = entityManager.createNativeQuery(
                "SELECT cvcontrato" +
                        " FROM controltomas" +
                        " WHERE fechacubre" +
                        " LIKE '____-__-__'" +
                        " AND (freinstala = '01/01/1900' OR freinstala = '1900-01-01' OR freinstala = '' OR freinstala IS null)", Integer.class);
        @SuppressWarnings("unchecked")
        List<Integer> resultFechaReinstalacionDefault = queryFechaReinstalacionDefault.getResultList();
        for (Integer cvcontrato : resultFechaReinstalacionDefault) {
            Query queryUpdate = entityManager.createNativeQuery("UPDATE controltomas SET freinstala = '1900-01-01' WHERE cvcontrato = ?");
            queryUpdate.setParameter(1, cvcontrato);
            queryUpdate.executeUpdate();
            count++;
            System.out.println(count);
        }

        System.out.println("step 4 complete");

        //2. apply format to second argument
        Query queryFechaReinstalacion = entityManager.createNativeQuery(
                "SELECT cvcontrato, freinstala" +
                        " FROM controltomas" +
                        " WHERE fechacubre" +
                        " LIKE '____-__-__'" +
                        " AND (freinstala <> '01/01/1900'" +
                        " AND freinstala <> '1900-01-01'" +
                        " AND freinstala <> ''" +
                        " AND freinstala IS NOT NULL)");
        @SuppressWarnings("unchecked")
        List<Object[]> resultFechaReinstalacion = queryFechaReinstalacion.getResultList();
        for (Object[] record : resultFechaReinstalacion) {
            Integer cvcontrato = (Integer) record[0];
            LocalDate fechaReinstalacion = LocalDate.parse((String) record[1], formatterReturn);
            Query query = entityManager.createNativeQuery("UPDATE controltomas SET freinstala = ? WHERE cvcontrato = ?");
            query.setParameter(1, fechaReinstalacion.format(formatter));
            query.setParameter(2, cvcontrato);
            query.executeUpdate();
            count++;
            System.out.println(count);
        }

        System.out.println("step 5 complete");

        //3. fix invalid dates with 13 in month subtracting one
        Query queryFixDates = entityManager.createNativeQuery("SELECT cvcontrato FROM controltomas WHERE fechacubre = '01/13/2022'", Integer.class);
        @SuppressWarnings("unchecked")
        List<Integer> resultFixDates = queryFixDates.getResultList();
        for (Integer cvcontrato : resultFixDates) {
            Query queryUpdate = entityManager.createNativeQuery("UPDATE controltomas SET fechacubre = '01/12/2022' WHERE cvcontrato = ?");
            queryUpdate.setParameter(1, cvcontrato);
            queryUpdate.executeUpdate();
            count++;
            System.out.println(count);
        }

        System.out.println("step 6 complete");

        //4. fix invalid dates with month format with just M rather MM
        Query queryFixDates2 = entityManager.createNativeQuery("SELECT cvcontrato, fechacubre FROM controltomas WHERE fechacubre LIKE '__/_[ ]/____'");
        @SuppressWarnings("unchecked")
        List<Object[]> resultFixDates2 = queryFixDates2.getResultList();
        for (Object[] record : resultFixDates2) {
            Integer cvcontrato = (Integer) record[0];
            String fechaCubre = (String) record[1];
            char[] fechaCubreCharArray = fechaCubre.toCharArray();
            MonthDay monthDay = MonthDay.of(Integer.parseInt(String.valueOf(fechaCubreCharArray[3])),
                    Integer.parseInt(String.valueOf(fechaCubreCharArray[0]) + String.valueOf(fechaCubreCharArray[1])));
            Year year = Year.of(Integer.parseInt(String.valueOf(fechaCubreCharArray[6]) +
                    String.valueOf(fechaCubreCharArray[7]) + String.valueOf(fechaCubreCharArray[8]) +
                    String.valueOf(fechaCubreCharArray[9])));
            LocalDate fechaCubreTyped = LocalDate.of(year.getValue(), monthDay.getMonth(), monthDay.getDayOfMonth());
            Query queryUpdate = entityManager.createNativeQuery("UPDATE controltomas SET fechacubre = ? WHERE cvcontrato = ?");
            queryUpdate.setParameter(1, fechaCubreTyped.format(formatter));
            queryUpdate.setParameter(2, cvcontrato);
            queryUpdate.executeUpdate();
            count++;
            System.out.println(count);
        }
        System.out.println("step 7 complete");

        //3.3. apply format to first argument, insert default value to second argument
        Query queryFechaReinstalacionYfechaCubre = entityManager.createNativeQuery(
                "SELECT cvcontrato, fechacubre" +
                        " FROM controltomas" +
                        " WHERE fechacubre" +
                        " LIKE '__/__/____'" +
                        " AND (freinstala = '01/01/1900' OR freinstala = '1900-01-01' OR freinstala = '' OR freinstala IS NULL)");
        @SuppressWarnings("unchecked")
        List<Object[]> resultFechaReinstalacionYfechaCubre = queryFechaReinstalacionYfechaCubre.getResultList();
        for (Object[] record : resultFechaReinstalacionYfechaCubre) {
            Integer cvcontrato = (Integer) record[0];
            LocalDate fechaCubre = LocalDate.parse((String) record[1], formatterReturn);
            Query queryUpdate = entityManager.createNativeQuery("UPDATE controltomas SET freinstala = '1900-01-01', fechacubre = ?  WHERE cvcontrato = ?");
            queryUpdate.setParameter(1, fechaCubre.format(formatter));
            queryUpdate.setParameter(2, cvcontrato);
            queryUpdate.executeUpdate();
            count++;
            System.out.println(count);
        }

        System.out.println("step 8 complete");

        //4. apply format to first and second argument
        Query queryFormatoFechaReinstalacionYfechaCubre = entityManager.createNativeQuery(
                "SELECT cvcontrato, fechacubre, freinstala" +
                        " FROM controltomas" +
                        " WHERE fechacubre LIKE '__/__/____'" +
                        " AND (freinstala <> '01/01/1900' AND freinstala <> '1900-01-01' AND freinstala <> '' AND freinstala IS NOT NULL)");
        @SuppressWarnings("unchecked")
        List<Object[]> resultFormatoFechaReinstalacionYfechaCubre = queryFormatoFechaReinstalacionYfechaCubre.getResultList();
        for (Object[] record : resultFormatoFechaReinstalacionYfechaCubre) {
            Integer cvcontrato = (Integer) record[0];
            LocalDate fechaCubre = LocalDate.parse((String) record[1], formatterReturn);
            LocalDate fechaReinstalacion = LocalDate.parse((String) record[2], formatterReturn);
            Query queryUpdate = entityManager.createNativeQuery("UPDATE controltomas SET fechacubre = ?, freinstala = ?  WHERE cvcontrato = ?");
            queryUpdate.setParameter(1, fechaCubre.format(formatter));
            queryUpdate.setParameter(2, fechaReinstalacion.format(formatter));
            queryUpdate.setParameter(3, cvcontrato);
            queryUpdate.executeUpdate();
            count++;
            System.out.println(count);
        }

        System.out.println("step 9 complete");


        //5. insert default value to both arguments
        Query queryFormatoFechaDefault = entityManager.createNativeQuery(
                "SELECT cvcontrato FROM controltomas WHERE fechacubre NOT LIKE '____-__-__' AND fechacubre NOT LIKE '__/__/____'", Integer.class);
        @SuppressWarnings("unchecked")
        List<Integer> resultFormatoFechaDefault = queryFormatoFechaDefault.getResultList();
        for (Integer cvcontrato : resultFormatoFechaDefault) {
            Query queryUpdate = entityManager.createNativeQuery("UPDATE controltomas SET fechacubre = '1900-01-01', freinstala = '1900-01-01'  WHERE cvcontrato = ?");
            queryUpdate.setParameter(1, cvcontrato);
            queryUpdate.executeUpdate();
            count++;
            System.out.println(count);
        }

        System.out.println("step 10 complete");
        return args -> {
        };
    }
}
