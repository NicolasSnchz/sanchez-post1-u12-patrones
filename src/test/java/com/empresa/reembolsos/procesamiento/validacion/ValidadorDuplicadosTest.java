package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.empresa.reembolsos.Solicitudes.pendiente;
import static com.empresa.reembolsos.Solicitudes.solicitud;
import static org.junit.jupiter.api.Assertions.*;

class ValidadorDuplicadosTest {

    private final ValidadorDuplicados validador = new ValidadorDuplicados();

    @Test
    void validar_conPendienteDeLaMismaCategoriaYMes_rechaza() {
        var pendientes = List.of(pendiente(CategoriaGasto.VIAJE, LocalDate.of(2026, 10, 2)));

        ResultadoValidacion resultado = validador.validar(solicitud(CategoriaGasto.VIAJE, "100000"), pendientes);

        assertFalse(resultado.valida());
        assertEquals("Ya existe una solicitud pendiente de la misma categoria en el periodo", resultado.motivoRechazo());
    }

    @Test
    void validar_conPendienteDeOtraCategoria_apruebaLaEtapa() {
        var pendientes = List.of(pendiente(CategoriaGasto.CAPACITACION, LocalDate.of(2026, 10, 2)));
        assertTrue(validador.validar(solicitud(CategoriaGasto.VIAJE, "100000"), pendientes).valida());
    }

    @Test
    void validar_conPendienteDeOtroMes_apruebaLaEtapa() {
        var pendientes = List.of(pendiente(CategoriaGasto.VIAJE, LocalDate.of(2026, 9, 30)));
        assertTrue(validador.validar(solicitud(CategoriaGasto.VIAJE, "100000"), pendientes).valida());
    }

    @Test
    void validar_conPendienteDelMismoMesDeOtroAnio_apruebaLaEtapa() {
        // octubre de 2025 y octubre de 2026 son periodos distintos; comparar solo getMonth() los confundia
        var pendientes = List.of(pendiente(CategoriaGasto.VIAJE, LocalDate.of(2025, 10, 15)));
        assertTrue(validador.validar(solicitud(CategoriaGasto.VIAJE, "100000"), pendientes).valida());
    }

    @Test
    void validar_sinPendientes_apruebaLaEtapa() {
        assertTrue(validador.validar(solicitud(CategoriaGasto.VIAJE, "100000"), List.of()).valida());
    }
}
