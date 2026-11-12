package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;

import java.util.List;

/**
 * Eslabon de la cadena de validacion (Chain of Responsibility). Cada validador revisa una sola
 * condicion: si la solicitud no la cumple, la cadena se detiene con su motivo; si la cumple, pasa
 * al siguiente eslabon sin saber cuantos vienen despues.
 */
public abstract class ValidadorSolicitud {
    private ValidadorSolicitud siguiente;

    /** Engancha el siguiente eslabon y lo devuelve, para poder encadenar las llamadas. */
    public ValidadorSolicitud siguiente(ValidadorSolicitud siguiente) {
        this.siguiente = siguiente;
        return siguiente;
    }

    public ResultadoValidacion validar(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientesDelEmpleado) {
        ResultadoValidacion propio = validarPropio(solicitud, pendientesDelEmpleado);
        if (!propio.valida() || siguiente == null) {
            return propio;
        }
        return siguiente.validar(solicitud, pendientesDelEmpleado);
    }

    /** La condicion propia de este eslabon. */
    protected abstract ResultadoValidacion validarPropio(
        SolicitudReembolso solicitud, List<SolicitudReembolso> pendientesDelEmpleado);
}
