package com.empresa.reembolsos.aplicacion;

public class SolicitudNoEncontradaException extends RuntimeException {
    public SolicitudNoEncontradaException(Long id) {
        super("Solicitud no encontrada: " + id);
    }
}
