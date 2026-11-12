package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static com.empresa.reembolsos.Solicitudes.solicitud;
import static org.junit.jupiter.api.Assertions.*;

class CalculadoraRepresentacionTest {

    private final CalculadoraRepresentacion calculadora = new CalculadoraRepresentacion();

    @ParameterizedTest(name = "solicitado {0}: aprobado {1}")
    @CsvSource({
        "1200000, 1200000",   // monto completo
        "2000000, 2000000",   // justo en el tope mensual
        "2500000, 2000000"    // por encima: el tope mensual
    })
    void calcular_reembolsaElMontoCompletoHastaElTopeMensual(String solicitado, String aprobado) {
        assertEquals(0, new BigDecimal(aprobado).compareTo(
            calculadora.calcular(solicitud(CategoriaGasto.REPRESENTACION, solicitado))));
    }
}
