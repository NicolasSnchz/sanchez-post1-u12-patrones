package com.empresa.reembolsos;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Fabrica de solicitudes para las pruebas: arma una solicitud valida que cada prueba ajusta. */
public final class Solicitudes {

    private Solicitudes() {
    }

    public static SolicitudReembolso solicitud(CategoriaGasto categoria, String monto) {
        SolicitudReembolso solicitud = new SolicitudReembolso();
        solicitud.setEmpleadoId("E-100");
        solicitud.setCategoria(categoria);
        solicitud.setMontoSolicitado(new BigDecimal(monto));
        solicitud.setFechaGasto(LocalDate.of(2026, 10, 15));
        solicitud.setDiasViaje(categoria == CategoriaGasto.VIAJE ? 3 : 0);
        solicitud.setDocumentosSoporte(true);
        return solicitud;
    }

    public static SolicitudReembolso pendiente(CategoriaGasto categoria, LocalDate fechaGasto) {
        SolicitudReembolso solicitud = solicitud(categoria, "100000");
        solicitud.setFechaGasto(fechaGasto);
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        return solicitud;
    }
}
