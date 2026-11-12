package com.empresa.reembolsos.aplicacion;

public class SolicitudInvalidaException extends RuntimeException {
    public SolicitudInvalidaException(String motivo) {
        super(motivo);
    }
}
