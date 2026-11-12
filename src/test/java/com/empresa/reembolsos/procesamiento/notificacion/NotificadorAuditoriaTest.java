package com.empresa.reembolsos.procesamiento.notificacion;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.LoggerFactory;

import static com.empresa.reembolsos.Solicitudes.solicitud;
import static org.junit.jupiter.api.Assertions.*;

class NotificadorAuditoriaTest {

    private final NotificadorAuditoria notificador = new NotificadorAuditoria();
    private final Logger logger = (Logger) LoggerFactory.getLogger(NotificadorAuditoria.class);
    private final ListAppender<ILoggingEvent> registro = new ListAppender<>();

    @BeforeEach
    void setUp() {
        registro.start();
        logger.addAppender(registro);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(registro);
    }

    @ParameterizedTest
    @EnumSource(EstadoSolicitud.class)
    void onCambioEstado_registraTodoCambioSinExcepcion(EstadoSolicitud nuevoEstado) {
        SolicitudReembolso solicitud = solicitud(CategoriaGasto.REPRESENTACION, "500000");
        solicitud.setId(7L);
        solicitud.setEstado(nuevoEstado);

        notificador.onCambioEstado(solicitud, EstadoSolicitud.PENDIENTE);

        assertEquals(1, registro.list.size());
        assertEquals("Auditoria: solicitud 7 cambio de PENDIENTE a " + nuevoEstado,
            registro.list.get(0).getFormattedMessage());
    }
}
