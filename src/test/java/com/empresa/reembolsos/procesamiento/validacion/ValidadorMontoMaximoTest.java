package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static com.empresa.reembolsos.Solicitudes.solicitud;
import static org.junit.jupiter.api.Assertions.*;

class ValidadorMontoMaximoTest {

    private final ValidadorMontoMaximo validador = new ValidadorMontoMaximo();

    @ParameterizedTest(name = "{0} por {1}: valida={2}")
    @CsvSource({
        "REPRESENTACION, 2000000, true",
        "REPRESENTACION, 2000001, false",
        "CAPACITACION, 3000000, true",
        "CAPACITACION, 3000001, false"
    })
    void validar_respetaElTopeDeCadaCategoria(CategoriaGasto categoria, String monto, boolean esperado) {
        assertEquals(esperado, validador.validar(solicitud(categoria, monto), List.of()).valida());
    }

    @ParameterizedTest(name = "viaje de {0} dias por {1}: valida={2}")
    @CsvSource({
        "3, 450000, true",
        "3, 450001, false",
        "3, 400000, true",
        "1, 150001, false"
    })
    void validar_enViajeElTopeEsDiarioPorLosDiasDelViaje(int dias, String monto, boolean esperado) {
        SolicitudReembolso viaje = solicitud(CategoriaGasto.VIAJE, monto);
        viaje.setDiasViaje(dias);
        assertEquals(esperado, validador.validar(viaje, List.of()).valida());
    }

    @Test
    void validar_sobreElTope_rechazaNombrandoLaCategoria() {
        ResultadoValidacion resultado = validador.validar(solicitud(CategoriaGasto.REPRESENTACION, "2500000"), List.of());
        assertEquals("Monto excede el tope de la categoria REPRESENTACION", resultado.motivoRechazo());
    }

    @ParameterizedTest
    @EnumSource(CategoriaGasto.class)
    void validar_todaCategoriaTieneUnTopeConfigurado(CategoriaGasto categoria) {
        // si alguien agrega una categoria sin su tope, esta prueba falla en vez de un NullPointerException en produccion
        assertTrue(validador.validar(solicitud(categoria, "1"), List.of()).valida());
    }
}
