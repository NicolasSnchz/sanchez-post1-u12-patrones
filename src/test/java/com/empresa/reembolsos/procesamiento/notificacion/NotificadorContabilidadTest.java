package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.Consola;
import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;

import static com.empresa.reembolsos.Solicitudes.solicitud;
import static org.junit.jupiter.api.Assertions.*;

class NotificadorContabilidadTest {

    private final NotificadorContabilidad notificador = new NotificadorContabilidad();

    @Test
    void onCambioEstado_aprobada_programaElPagoDelMontoAprobado() {
        SolicitudReembolso aprobada = solicitud(CategoriaGasto.CAPACITACION, "1000000");
        aprobada.setMontoAprobado(new BigDecimal("800000"));
        aprobada.setEstado(EstadoSolicitud.APROBADA);

        String salida = Consola.durante(() -> notificador.onCambioEstado(aprobada, EstadoSolicitud.PENDIENTE));

        assertEquals("Contabilidad: programar pago de 800000 para E-100" + System.lineSeparator(), salida);
    }

    @ParameterizedTest
    @EnumSource(value = EstadoSolicitud.class, names = "APROBADA", mode = EnumSource.Mode.EXCLUDE)
    void onCambioEstado_cualquierOtroEstado_noHaceNada(EstadoSolicitud nuevoEstado) {
        SolicitudReembolso solicitud = solicitud(CategoriaGasto.CAPACITACION, "1000000");
        solicitud.setEstado(nuevoEstado);

        assertEquals("", Consola.durante(() -> notificador.onCambioEstado(solicitud, EstadoSolicitud.PENDIENTE)));
    }
}
