package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.empresa.reembolsos.Solicitudes.pendiente;
import static com.empresa.reembolsos.Solicitudes.solicitud;
import static org.junit.jupiter.api.Assertions.*;

class CadenaValidadoresTest {

    private final CadenaValidadores cadena = new CadenaValidadores(
        new ValidadorDocumentosSoporte(), new ValidadorMontoMaximo(), new ValidadorDuplicados());

    private final List<SolicitudReembolso> conDuplicado =
        List.of(pendiente(CategoriaGasto.REPRESENTACION, LocalDate.of(2026, 10, 1)));

    @Test
    void validar_solicitudQueCumpleLasTresEtapas_esValida() {
        assertTrue(cadena.validar(solicitud(CategoriaGasto.REPRESENTACION, "500000"), List.of()).valida());
    }

    @Test
    void validar_conVariasFallas_reportaLaDelPrimerEslabon() {
        // falla en las tres etapas: el motivo es el de documentos porque es el primer eslabon
        SolicitudReembolso todoMal = solicitud(CategoriaGasto.REPRESENTACION, "9000000");
        todoMal.setDocumentosSoporte(false);

        assertEquals("Faltan documentos soporte", cadena.validar(todoMal, conDuplicado).motivoRechazo());
    }

    @Test
    void validar_montoExcedidoYDuplicado_seDetieneEnElMontoSinLlegarADuplicados() {
        assertEquals("Monto excede el tope de la categoria REPRESENTACION",
            cadena.validar(solicitud(CategoriaGasto.REPRESENTACION, "9000000"), conDuplicado).motivoRechazo());
    }

    @Test
    void validar_soloDuplicado_llegaAlUltimoEslabon() {
        assertEquals("Ya existe una solicitud pendiente de la misma categoria en el periodo",
            cadena.validar(solicitud(CategoriaGasto.REPRESENTACION, "500000"), conDuplicado).motivoRechazo());
    }
}
