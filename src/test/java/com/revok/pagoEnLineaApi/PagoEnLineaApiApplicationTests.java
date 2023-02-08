package com.revok.pagoEnLineaApi;

import com.revok.pagoEnLineaApi.controller.ContratoController;
import com.revok.pagoEnLineaApi.model.Departamento;
import com.revok.pagoEnLineaApi.model.Deuda;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootTest
class PagoEnLineaApiApplicationTests {

	@Test
	void contratoWithMedidor(@Autowired ContratoController contratoController) {
		Deuda deuda = contratoController.findDeudaContrato("8002", Departamento.CENTRO.name(), 3).getBody();
		Assertions.assertEquals(BigDecimal.valueOf(490.72), deuda.getTotalCuotaOConsumo(), "Consumo");
		Assertions.assertEquals(BigDecimal.valueOf(147.22), deuda.getTotalSaneamiento(), "Saneamiento");
		Assertions.assertNotNull(deuda.getTotalPagar());
		Assertions.assertEquals(BigDecimal.valueOf(651.76), deuda.getTotalPagar(), "Total A pagar");
	}

	@Test
	void contratoWithCuotaFija(@Autowired ContratoController contratoController) {
        Deuda deuda = contratoController.findDeudaContrato("449", Departamento.CENTRO.name(), 26).getBody();
        Assertions.assertEquals(LocalDate.of(2023, 2, 28), deuda.getFechaCubre(), "Fecha cubre");
        Assertions.assertEquals(BigDecimal.valueOf(2681.07), deuda.getTotalCuotaOConsumo(), "Consumo");
        Assertions.assertEquals(BigDecimal.valueOf(1072.45), deuda.getTotalSaneamiento(), "Saneamiento");
        Assertions.assertNotNull(deuda.getTotalPagar());
        Assertions.assertEquals(BigDecimal.valueOf(4057.93), deuda.getTotalPagar(), "Total A pagar");
    }
}
