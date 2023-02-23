package com.revok.pagoEnLineaApi;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
    @SuppressWarnings("unused")
    public CommandLineRunner initApp(EntityManager entityManager, @Value("${revok.normalization}") Boolean normalization) {
        if (!normalization) {
            return args -> {
            };
        }

        ZoneId defaultZoneId = ZoneId.of("America/Mexico_City");
        Locale defaultLocale = new Locale("spa", "MX");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", defaultLocale); //23
        DateTimeFormatter formatterReturn = DateTimeFormatter.ofPattern("dd/MM/yyyy", defaultLocale); //103
        DateTimeFormatter formatterDateOnly = DateTimeFormatter.ofPattern("yyyyMMdd", defaultLocale); //112

        System.out.println("standardization begins");

        int count = 0;

        // normalización 1: aplicación de formato estándar de fecha (yyyy-mm-dd) sobre fecantes y feclectura en lecturas*
        try {
            Query queryLecturas = entityManager.createNativeQuery("SELECT cvlectura, fecantes, feclectura FROM lecturas WHERE feclectura LIKE '__/__/____'");
            @SuppressWarnings("unchecked")
            List<Object[]> resultLecturas = queryLecturas.getResultList();
            Query updateLecturas = entityManager.createNativeQuery("UPDATE lecturas SET fecantes = ?, feclectura = ? WHERE cvlectura = ?");
            for (Object[] record : resultLecturas) {
                Integer cvlectura = (Integer) record[0];
                LocalDate fechaAntes = LocalDate.parse((String) record[1], formatterReturn);
                LocalDate fechaLectura = LocalDate.parse((String) record[2], formatterReturn);
                updateLecturas.setParameter(1, fechaAntes.format(formatter));
                updateLecturas.setParameter(2, fechaLectura.format(formatter));
                updateLecturas.setParameter(3, cvlectura);
                updateLecturas.executeUpdate();
                count++;
                System.out.println(count);
            }

            System.out.println("step 1 complete");
        } catch (Exception e) {
            System.out.println("step 1 complete" + e.getLocalizedMessage());
        }

        // normalización 2: numfamilia < 1 => 1. aplicado a tabla dattomas*
        try {
            Query queryNumFamilias = entityManager.createNativeQuery("SELECT cvcontrato FROM dattomas WHERE numfamilia < 1");
            @SuppressWarnings("unchecked")
            List<Integer> resultNumFamilia = queryNumFamilias.getResultList();
            Query updateNumFamilias = entityManager.createNativeQuery("UPDATE dattomas SET numfamilia = 1 WHERE cvcontrato = ?");
            for (Integer cvcontrato : resultNumFamilia) {
                updateNumFamilias.setParameter(1, cvcontrato);
                updateNumFamilias.executeUpdate();
                count++;
                System.out.println(count);
            }

            System.out.println("step 2 complete");
        } catch (Exception e) {
            System.out.println("step 2 complete with error: " + e.getLocalizedMessage());
        }

        int countPagoGlobal = 0;
        // Normalización 3: formato de fecha en pagoGlobal.*
        try {
            Query queryFechasPagoGlobal = entityManager.createNativeQuery(
                    "SELECT cvcontrato, fechareg, fcubre, fechaantes, numrecibo" +
                            " FROM pagoglobal" +
                            " WHERE fechareg LIKE '__/__/____'" +
                            " AND fcubre LIKE '__/__/____'" +
                            " AND fechaantes LIKE '__/__/____'");
            @SuppressWarnings("unchecked")
            List<Object[]> resultFechasPagoGlobal = queryFechasPagoGlobal.getResultList();
            Query updateFechasPagoGlobal = entityManager.createNativeQuery(
                    "UPDATE pagoglobal SET fechareg = ?, fcubre = ?, fechaantes = ? WHERE cvcontrato = ? AND fechareg = ? AND numrecibo = ?");
            for (Object[] record : resultFechasPagoGlobal) {
                String cvcontrato = (String) record[0];
                String fechaRegistro = (String) record[1];
                LocalDate fechaRegistroTyped = LocalDate.parse(fechaRegistro, formatterReturn);
                LocalDate fechaCubre = LocalDate.parse((String) record[2], formatterReturn);
                LocalDate fechaAntes = LocalDate.parse((String) record[3], formatterReturn);
                Integer numeroRecibo = (Integer) record[4];
                updateFechasPagoGlobal.setParameter(1, fechaRegistroTyped.format(formatter));
                updateFechasPagoGlobal.setParameter(2, fechaCubre.format(formatter));
                updateFechasPagoGlobal.setParameter(3, fechaAntes.format(formatter));
                updateFechasPagoGlobal.setParameter(4, cvcontrato);
                updateFechasPagoGlobal.setParameter(5, fechaRegistro);
                updateFechasPagoGlobal.setParameter(6, numeroRecibo);
                updateFechasPagoGlobal.executeUpdate();
                count++;
                countPagoGlobal++;
                System.out.println(countPagoGlobal);
            }

            System.out.println("step 3 complete");
        } catch (Exception e) {
            System.out.println("step 3 complete with error: " + e.getLocalizedMessage());
        }

        // Normalización 3.1: formato de fecha en pagoGlobal.*
        try {
            System.out.println("step 3.1 begins");
            Query queryFechasPagoGlobal = entityManager.createNativeQuery(
                    "SELECT cvcontrato, fechareg, fcubre, fechaantes, numrecibo" +
                            " FROM pagoglobal" +
                            " WHERE fechareg LIKE '__/__/____'" +
                            " AND fcubre LIKE '________'" +
                            " AND fechaantes LIKE '________'");
            System.out.println("query begins");
            @SuppressWarnings("unchecked")
            List<Object[]> resultFechasPagoGlobal = queryFechasPagoGlobal.getResultList();
            System.out.println("query finish");
            Query updateFechasPagoGlobal = entityManager.createNativeQuery(
                    "UPDATE pagoglobal SET fechareg = ?, fcubre = ?, fechaantes = ? WHERE cvcontrato = ? AND fechareg = ? AND numrecibo = ?");
            for (Object[] record : resultFechasPagoGlobal) {
                String cvcontrato = (String) record[0];
                String fechaRegistro = (String) record[1];
                LocalDate fechaRegistroTyped = LocalDate.parse(fechaRegistro, formatterReturn);
                LocalDate fechaCubre = LocalDate.parse((String) record[2], formatterDateOnly);
                LocalDate fechaAntes = LocalDate.parse((String) record[3], formatterDateOnly);
                Integer numeroRecibo = (Integer) record[4];
                updateFechasPagoGlobal.setParameter(1, fechaRegistroTyped.format(formatter));
                updateFechasPagoGlobal.setParameter(2, fechaCubre.format(formatter));
                updateFechasPagoGlobal.setParameter(3, fechaAntes.format(formatter));
                updateFechasPagoGlobal.setParameter(4, cvcontrato);
                updateFechasPagoGlobal.setParameter(5, fechaRegistro);
                updateFechasPagoGlobal.setParameter(6, numeroRecibo);
                updateFechasPagoGlobal.executeUpdate();
                count++;
                countPagoGlobal++;
                System.out.println(countPagoGlobal);
            }

            System.out.println("step 3.1 complete");
        } catch (Exception e) {
            System.out.println("step 3.1 complete with error: " + e.getLocalizedMessage());
            System.out.println("step 3.1 complete with error: " + e.getMessage());
            e.printStackTrace();
        }

        // Normalización 3.2: formato de fecha en pagoGlobal.*
        try {
            Query queryFechasPagoGlobal = entityManager.createNativeQuery(
                    "SELECT cvcontrato, fechareg, fcubre, fechaantes, numrecibo" +
                            " FROM pagoglobal" +
                            " WHERE fechareg LIKE '__/__/____'" +
                            " AND fcubre LIKE '________'" +
                            " AND fechaantes LIKE '__/__/____'");
            @SuppressWarnings("unchecked")
            List<Object[]> resultFechasPagoGlobal = queryFechasPagoGlobal.getResultList();
            Query updateFechasPagoGlobal = entityManager.createNativeQuery(
                    "UPDATE pagoglobal SET fechareg = ?, fcubre = ?, fechaantes = ? WHERE cvcontrato = ? AND fechareg = ? AND numrecibo = ?");
            for (Object[] record : resultFechasPagoGlobal) {
                String cvcontrato = (String) record[0];
                String fechaRegistro = (String) record[1];
                LocalDate fechaRegistroTyped = LocalDate.parse(fechaRegistro, formatterReturn);
                LocalDate fechaCubre = LocalDate.parse((String) record[2], formatterDateOnly);
                LocalDate fechaAntes = LocalDate.parse((String) record[3], formatterReturn);
                Integer numeroRecibo = (Integer) record[4];
                updateFechasPagoGlobal.setParameter(1, fechaRegistroTyped.format(formatter));
                updateFechasPagoGlobal.setParameter(2, fechaCubre.format(formatter));
                updateFechasPagoGlobal.setParameter(3, fechaAntes.format(formatter));
                updateFechasPagoGlobal.setParameter(4, cvcontrato);
                updateFechasPagoGlobal.setParameter(5, fechaRegistro);
                updateFechasPagoGlobal.setParameter(6, numeroRecibo);
                updateFechasPagoGlobal.executeUpdate();
                count++;
                countPagoGlobal++;
                System.out.println(countPagoGlobal);
            }

            System.out.println("step 3.2 complete");
        } catch (Exception e) {
            System.out.println("step 3.2 complete with error: " + e.getLocalizedMessage());
        }


        System.out.println("countPagoGlobal: " + countPagoGlobal);
        // Normalización 4: formato de fecha en fechacubre y freinstala para controltomas*
        //1. insert default value to second argument
        try {
            Query queryFechaReinstalacionDefault = entityManager.createNativeQuery(
                    "SELECT cvcontrato" +
                            " FROM controltomas" +
                            " WHERE fechacubre" +
                            " LIKE '____-__-__'" +
                            " AND (freinstala = '01/01/1900' OR freinstala = '1900-01-01' OR freinstala = '' OR freinstala IS null)", Integer.class);
            @SuppressWarnings("unchecked")
            List<Integer> resultFechaReinstalacionDefault = queryFechaReinstalacionDefault.getResultList();
            Query updateFechaReinstalacionDefault = entityManager.createNativeQuery("UPDATE controltomas SET freinstala = '1900-01-01' WHERE cvcontrato = ?");
            for (Integer cvcontrato : resultFechaReinstalacionDefault) {
                updateFechaReinstalacionDefault.setParameter(1, cvcontrato);
                updateFechaReinstalacionDefault.executeUpdate();
                count++;
                System.out.println(count);
            }

            System.out.println("step 4 complete");
        } catch (Exception e) {
            System.out.println("step 4 complete with error: " + e.getLocalizedMessage());
        }

        //2. apply format to second argument*
        try {
            Query queryFechaReinstalacion = entityManager.createNativeQuery(
                    "SELECT cvcontrato, freinstala" +
                            " FROM controltomas" +
                            " WHERE fechacubre" +
                            " LIKE '____-__-__'" +
                            " AND freinstala LIKE '__/__/____'" +
                            " AND (freinstala <> '01/01/1900'" +
                            " AND freinstala <> '1900-01-01'" +
                            " AND freinstala <> ''" +
                            " AND freinstala IS NOT NULL)");
            @SuppressWarnings("unchecked")
            List<Object[]> resultFechaReinstalacion = queryFechaReinstalacion.getResultList();
            Query updateFechaReinstalacion = entityManager.createNativeQuery("UPDATE controltomas SET freinstala = ? WHERE cvcontrato = ?");
            for (Object[] record : resultFechaReinstalacion) {
                Integer cvcontrato = (Integer) record[0];
                LocalDate fechaReinstalacion = LocalDate.parse((String) record[1], formatterReturn);
                updateFechaReinstalacion.setParameter(1, fechaReinstalacion.format(formatter));
                updateFechaReinstalacion.setParameter(2, cvcontrato);
                updateFechaReinstalacion.executeUpdate();
                count++;
                System.out.println(count);
            }

            System.out.println("step 5 complete");
        } catch (Exception e) {
            System.out.println("step 5 complete with error: " + e.getLocalizedMessage());
        }

        //3. fix invalid dates with 13 in month subtracting one*
        try {
            Query queryFixDates = entityManager.createNativeQuery("SELECT cvcontrato FROM controltomas WHERE fechacubre = '01/13/2022'", Integer.class);
            @SuppressWarnings("unchecked")
            List<Integer> resultFixDates = queryFixDates.getResultList();
            Query updateFixDates = entityManager.createNativeQuery("UPDATE controltomas SET fechacubre = '01/12/2022' WHERE cvcontrato = ?");
            for (Integer cvcontrato : resultFixDates) {
                updateFixDates.setParameter(1, cvcontrato);
                updateFixDates.executeUpdate();
                count++;
                System.out.println(count);
            }

            System.out.println("step 6 complete");
        } catch (Exception e) {
            System.out.println("steop 6 complete with error: " + e.getLocalizedMessage());
        }

        //4. fix invalid dates with month format with just M rather MM*
        try {
            Query queryFixDates2 = entityManager.createNativeQuery("SELECT cvcontrato, fechacubre FROM controltomas WHERE fechacubre LIKE '__/_[ ]/____'");
            @SuppressWarnings("unchecked")
            List<Object[]> resultFixDates2 = queryFixDates2.getResultList();
            Query updateFixDates2 = entityManager.createNativeQuery("UPDATE controltomas SET fechacubre = ? WHERE cvcontrato = ?");
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
                updateFixDates2.setParameter(1, fechaCubreTyped.format(formatter));
                updateFixDates2.setParameter(2, cvcontrato);
                updateFixDates2.executeUpdate();
                count++;
                System.out.println(count);
            }
            System.out.println("step 7 complete");
        } catch (Exception e) {
            System.out.println("step 7 complete with error: " + e.getLocalizedMessage());
        }

        //3.3. apply format to fechacubre on controltomas*
        try {
            Query queryFechaReinstalacionYfechaCubre = entityManager.createNativeQuery(
                    "SELECT cvcontrato, fechacubre" +
                            " FROM controltomas" +
                            " WHERE fechacubre" +
                            " LIKE '__/__/____'" +
                            " AND (freinstala = '01/01/1900' OR freinstala = '1900-01-01' OR freinstala = '' OR freinstala IS NULL)");
            @SuppressWarnings("unchecked")
            List<Object[]> resultFechaReinstalacionYfechaCubre = queryFechaReinstalacionYfechaCubre.getResultList();
            Query updateFechaReinstalacionYfechaCubre = entityManager.createNativeQuery("UPDATE controltomas SET freinstala = '1900-01-01', fechacubre = ? WHERE cvcontrato = ?");
            for (Object[] record : resultFechaReinstalacionYfechaCubre) {
                Integer cvcontrato = (Integer) record[0];
                LocalDate fechaCubre = LocalDate.parse((String) record[1], formatterReturn);
                updateFechaReinstalacionYfechaCubre.setParameter(1, fechaCubre.format(formatter));
                updateFechaReinstalacionYfechaCubre.setParameter(2, cvcontrato);
                updateFechaReinstalacionYfechaCubre.executeUpdate();
                count++;
                System.out.println(count);
            }

            System.out.println("step 8 complete");
        } catch (Exception e) {
            System.out.println("step 8 complete with error: " + e.getLocalizedMessage());
        }

        //4. apply format to fechacubre, freinstala on controltomas*
        try {
            Query queryFormatoFechaReinstalacionYfechaCubre = entityManager.createNativeQuery(
                    "SELECT cvcontrato, fechacubre, freinstala" +
                            " FROM controltomas" +
                            " WHERE fechacubre LIKE '__/__/____'" +
                            " AND (freinstala <> '01/01/1900' AND freinstala <> '1900-01-01' AND freinstala <> '' AND freinstala IS NOT NULL)");
            @SuppressWarnings("unchecked")
            List<Object[]> resultFormatoFechaReinstalacionYfechaCubre = queryFormatoFechaReinstalacionYfechaCubre.getResultList();
            Query updateFormatoFechaReinstalacionYfechaCubre = entityManager.createNativeQuery("UPDATE controltomas SET fechacubre = ?, freinstala = ? WHERE cvcontrato = ?");
            for (Object[] record : resultFormatoFechaReinstalacionYfechaCubre) {
                Integer cvcontrato = (Integer) record[0];
                LocalDate fechaCubre = LocalDate.parse((String) record[1], formatterReturn);
                LocalDate fechaReinstalacion = LocalDate.parse((String) record[2], formatterReturn);
                updateFormatoFechaReinstalacionYfechaCubre.setParameter(1, fechaCubre.format(formatter));
                updateFormatoFechaReinstalacionYfechaCubre.setParameter(2, fechaReinstalacion.format(formatter));
                updateFormatoFechaReinstalacionYfechaCubre.setParameter(3, cvcontrato);
                updateFormatoFechaReinstalacionYfechaCubre.executeUpdate();
                count++;
                System.out.println(count);
            }

            System.out.println("step 9 complete");
        } catch (Exception e) {
            System.out.println("step 9 complete with error: " + e.getLocalizedMessage());
        }


        //5. insert default value to fechacubre, freinstala on controltomas*
        try {
            Query queryFormatoFechaDefault = entityManager.createNativeQuery(
                    "SELECT cvcontrato FROM controltomas WHERE fechacubre NOT LIKE '____-__-__' AND fechacubre NOT LIKE '__/__/____'", Integer.class);
            @SuppressWarnings("unchecked")
            List<Integer> resultFormatoFechaDefault = queryFormatoFechaDefault.getResultList();
            Query updateFormatoFechaDefault = entityManager.createNativeQuery("UPDATE controltomas SET fechacubre = '1900-01-01', freinstala = '1900-01-01' WHERE cvcontrato = ?");
            for (Integer cvcontrato : resultFormatoFechaDefault) {
                updateFormatoFechaDefault.setParameter(1, cvcontrato);
                updateFormatoFechaDefault.executeUpdate();
                count++;
                System.out.println(count);
            }

            System.out.println("step 10 complete");
        } catch (Exception e) {
            System.out.println("step 10 complete with error: " + e.getLocalizedMessage());
        }

        //6. giros apply date format on fecvigencia & fechasaneamiento using cvgiros as row reference*
        try {
            Query queryFormatoFechaGiros = entityManager.createNativeQuery("SELECT cvgiros, fecvigencia, fechasaneamiento FROM giros");
            @SuppressWarnings("unchecked")
            List<Object[]> resultFormatoFechaGiros = queryFormatoFechaGiros.getResultList();
            Query updateFormatoFechaGiros = entityManager.createNativeQuery("UPDATE giros SET fecvigencia = ?, fechasaneamiento = ? WHERE cvgiros = ?");
            for (Object[] record : resultFormatoFechaGiros) {
                Integer cvgiro = (Integer) record[0];
                LocalDate fechaVigencia = LocalDate.parse((String) record[1], formatterReturn);
                LocalDate fechaSaneamiento = LocalDate.parse((String) record[2], formatterReturn);
                updateFormatoFechaGiros.setParameter(1, fechaVigencia.format(formatter));
                updateFormatoFechaGiros.setParameter(2, fechaSaneamiento.format(formatter));
                updateFormatoFechaGiros.setParameter(3, cvgiro);
                updateFormatoFechaGiros.executeUpdate();
                count++;
                System.out.println(count);
            }

            System.out.println("step 11 complete");
        } catch (Exception e) {
            System.out.println("step 11 complete with error: " + e.getLocalizedMessage());
        }

        //7. datosagua apply date format on fechadetiene on all rows (there's always unique record)
        try {
            Query queryFormatoFechaDatosAgua = entityManager.createNativeQuery("SELECT fechadetiene FROM datosagua", String.class);
            @SuppressWarnings("unchecked")
            List<String> resultFormatoFechaDatosAgua = queryFormatoFechaDatosAgua.getResultList();
            Query updateFormatoFechaDatosAgua = entityManager.createNativeQuery("UPDATE datosagua SET fechadetiene = ?");
            for (String fecha : resultFormatoFechaDatosAgua) {
                LocalDate fechaTyped = LocalDate.parse(fecha, formatterReturn);
                updateFormatoFechaDatosAgua.setParameter(1, fechaTyped.format(formatter));
                updateFormatoFechaDatosAgua.executeUpdate();
                count++;
                System.out.println(count);
            }

            System.out.println("step 12 complete");
        } catch (Exception e) {
            System.out.println("step 12 complete with error: " + e.getLocalizedMessage());
        }
        return args -> {
        };
    }
}
