package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.Consola;
import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.empresa.reembolsos.Solicitudes.solicitud;
import static org.junit.jupiter.api.Assertions.*;

class NotificadorEmpleadoTest {

    private final NotificadorEmpleado notificador = new NotificadorEmpleado();

    @ParameterizedTest
    @EnumSource(value = EstadoSolicitud.class, names = "PENDIENTE", mode = EnumSource.Mode.EXCLUDE)
    void onCambioEstado_avisaAlEmpleadoElResultadoSeaCualSea(EstadoSolicitud nuevoEstado) {
        SolicitudReembolso solicitud = solicitud(CategoriaGasto.VIAJE, "100000");
        solicitud.setEstado(nuevoEstado);

        String salida = Consola.durante(() -> notificador.onCambioEstado(solicitud, EstadoSolicitud.PENDIENTE));

        assertEquals("Notificando a E-100: su solicitud paso de PENDIENTE a " + nuevoEstado
            + System.lineSeparator(), salida);
    }
}
