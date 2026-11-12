package com.empresa.reembolsos;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import com.empresa.reembolsos.procesamiento.notificacion.ObservadorCambioEstado;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Flujo completo por HTTP con el contexto real: validacion, calculo, persistencia en H2 y notificacion
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class SolicitudesIntegracionTest {

    // Un cuarto interesado registrado solo para la prueba: demuestra que el servicio avisa a
    // todos los observadores sin conocerlos, igual que pasaria con el panel de metricas
    @TestConfiguration
    static class ObservadorDePrueba {
        @Bean
        RegistroDeAvisos registroDeAvisos() {
            return new RegistroDeAvisos();
        }
    }

    static class RegistroDeAvisos implements ObservadorCambioEstado {
        final List<String> avisos = new ArrayList<>();

        @Override
        public void onCambioEstado(SolicitudReembolso solicitud, EstadoSolicitud estadoAnterior) {
            avisos.add(estadoAnterior + " -> " + solicitud.getEstado());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RegistroDeAvisos registro;

    @BeforeEach
    void setUp() {
        registro.avisos.clear();
    }

    @ParameterizedTest(name = "{0}: solicitado {1}, aprobado {3}")
    @CsvSource({
        "VIAJE, 400000, 3, 400000",
        "REPRESENTACION, 1200000, 0, 1200000",
        "CAPACITACION, 1000000, 0, 800000"
    })
    void crear_lasTresCategorias_calculaElMontoAprobadoYQuedaPendiente(
            String categoria, String monto, int dias, String aprobado) throws Exception {
        JsonNode creada = crear(json("E-" + categoria, categoria, monto, dias, true));

        assertEquals(0, new BigDecimal(aprobado).compareTo(creada.get("montoAprobado").decimalValue()));
        assertEquals("PENDIENTE", creada.get("estado").asText());
        assertTrue(creada.get("id").asLong() > 0);
    }

    @Test
    void crear_sinDocumentosSoporte_respondeBadRequestConElMotivo() throws Exception {
        mockMvc.perform(post("/api/solicitudes").contentType(MediaType.APPLICATION_JSON)
                .content(json("E-SIN-DOCS", "VIAJE", "100000", 1, false)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Faltan documentos soporte"));
    }

    @Test
    void crear_segundaSolicitudPendienteDelMismoPeriodo_respondeBadRequest() throws Exception {
        crear(json("E-DUPLICADO", "REPRESENTACION", "300000", 0, true));

        mockMvc.perform(post("/api/solicitudes").contentType(MediaType.APPLICATION_JSON)
                .content(json("E-DUPLICADO", "REPRESENTACION", "200000", 0, true)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Ya existe una solicitud pendiente de la misma categoria en el periodo"));
    }

    @Test
    void cambiarEstado_aprobada_avisaALosInteresadosYContabilidadProgramaElPago(CapturedOutput salida) throws Exception {
        long id = crear(json("E-APROBADA", "CAPACITACION", "1000000", 0, true)).get("id").asLong();

        cambiarEstado(id, "APROBADA").andExpect(jsonPath("$.estado").value("APROBADA"));

        assertEquals(List.of("PENDIENTE -> APROBADA"), registro.avisos);
        assertTrue(salida.getOut().contains("Notificando a E-APROBADA: su solicitud paso de PENDIENTE a APROBADA"));
        assertTrue(salida.getOut().contains("Contabilidad: programar pago de 800000"));
        assertTrue(salida.getOut().contains("Auditoria: solicitud " + id + " cambio de PENDIENTE a APROBADA"));
    }

    @Test
    void cambiarEstado_rechazada_avisaPeroContabilidadNoActua(CapturedOutput salida) throws Exception {
        long id = crear(json("E-RECHAZADA", "VIAJE", "100000", 1, true)).get("id").asLong();

        cambiarEstado(id, "RECHAZADA");

        assertEquals(List.of("PENDIENTE -> RECHAZADA"), registro.avisos);
        assertTrue(salida.getOut().contains("Notificando a E-RECHAZADA: su solicitud paso de PENDIENTE a RECHAZADA"));
        assertFalse(salida.getOut().contains("programar pago"));
    }

    @Test
    void cambiarEstado_repetirElMismoEstado_noAvisaDosVeces() throws Exception {
        long id = crear(json("E-REPETIDA", "REPRESENTACION", "400000", 0, true)).get("id").asLong();

        cambiarEstado(id, "APROBADA");
        cambiarEstado(id, "APROBADA");

        assertEquals(List.of("PENDIENTE -> APROBADA"), registro.avisos);
    }

    @Test
    void cambiarEstado_solicitudInexistente_respondeNotFound() throws Exception {
        mockMvc.perform(put("/api/solicitudes/9999/estado").contentType(MediaType.APPLICATION_JSON)
                .content("\"APROBADA\""))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Solicitud no encontrada: 9999"));
    }

    private JsonNode crear(String cuerpo) throws Exception {
        String respuesta = mockMvc.perform(post("/api/solicitudes").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(respuesta);
    }

    private ResultActions cambiarEstado(long id, String estado) throws Exception {
        return mockMvc.perform(put("/api/solicitudes/" + id + "/estado").contentType(MediaType.APPLICATION_JSON)
                .content("\"" + estado + "\""))
            .andExpect(status().isOk());
    }

    private static String json(String empleado, String categoria, String monto, int dias, boolean documentos) {
        return """
            {"empleadoId": "%s", "categoria": "%s", "montoSolicitado": %s,
             "fechaGasto": "2026-10-15", "diasViaje": %d, "documentosSoporte": %b}
            """.formatted(empleado, categoria, monto, dias, documentos);
    }
}
