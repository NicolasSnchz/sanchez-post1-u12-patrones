package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static com.empresa.reembolsos.Solicitudes.solicitud;
import static org.junit.jupiter.api.Assertions.*;

class CalculadoraViajeTest {

    private final CalculadoraViaje calculadora = new CalculadoraViaje();

    @ParameterizedTest(name = "{0} dias, solicitado {1}: aprobado {2}")
    @CsvSource({
        "3, 400000, 400000",   // por debajo del tope: se aprueba completo
        "3, 450000, 450000",   // justo en el tope de 150.000 x 3 dias
        "3, 600000, 450000",   // por encima: se aprueba solo el tope
        "1, 200000, 150000"
    })
    void calcular_aplicaElTopeDiarioPorLosDiasDelViaje(int dias, String solicitado, String aprobado) {
        SolicitudReembolso viaje = solicitud(CategoriaGasto.VIAJE, solicitado);
        viaje.setDiasViaje(dias);
        assertEquals(0, new BigDecimal(aprobado).compareTo(calculadora.calcular(viaje)));
    }
}
