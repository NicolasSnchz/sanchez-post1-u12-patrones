package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

/**
 * Interesado en los cambios de estado de una solicitud (Observer). El servicio avisa a todos los
 * observadores registrados y cada uno decide como reaccionar; un interesado nuevo es una clase
 * nueva que implementa esta interfaz.
 */
public interface ObservadorCambioEstado {

    /** Se invoca despues de guardar el nuevo estado, que ya viene en la solicitud. */
    void onCambioEstado(SolicitudReembolso solicitud, EstadoSolicitud estadoAnterior);
}
