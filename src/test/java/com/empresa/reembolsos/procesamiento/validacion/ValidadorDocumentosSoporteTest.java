package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.empresa.reembolsos.Solicitudes.solicitud;
import static org.junit.jupiter.api.Assertions.*;

class ValidadorDocumentosSoporteTest {

    private final ValidadorDocumentosSoporte validador = new ValidadorDocumentosSoporte();

    @Test
    void validar_conDocumentosSoporte_apruebaLaEtapa() {
        assertTrue(validador.validar(solicitud(CategoriaGasto.VIAJE, "100000"), List.of()).valida());
    }

    @Test
    void validar_sinDocumentosSoporte_rechazaConMotivo() {
        SolicitudReembolso sinDocumentos = solicitud(CategoriaGasto.VIAJE, "100000");
        sinDocumentos.setDocumentosSoporte(false);

        ResultadoValidacion resultado = validador.validar(sinDocumentos, List.of());

        assertFalse(resultado.valida());
        assertEquals("Faltan documentos soporte", resultado.motivoRechazo());
    }
}
