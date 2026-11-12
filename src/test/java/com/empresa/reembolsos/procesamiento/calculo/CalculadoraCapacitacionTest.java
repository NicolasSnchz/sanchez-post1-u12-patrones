package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static com.empresa.reembolsos.Solicitudes.solicitud;
import static org.junit.jupiter.api.Assertions.*;

class CalculadoraCapacitacionTest {

    private final CalculadoraCapacitacion calculadora = new CalculadoraCapacitacion();

    @ParameterizedTest(name = "solicitado {0}: aprobado {1}")
    @CsvSource({
        "1000000, 800000",     // el 80 %
        "3750000, 3000000",    // el 80 % llega justo al tope anual
        "5000000, 3000000"     // el 80 % (4.000.000) supera el tope: se aprueba el tope
    })
    void calcular_reembolsaElOchentaPorCientoHastaElTopeAnual(String solicitado, String aprobado) {
        assertEquals(0, new BigDecimal(aprobado).compareTo(
            calculadora.calcular(solicitud(CategoriaGasto.CAPACITACION, solicitado))));
    }
}
