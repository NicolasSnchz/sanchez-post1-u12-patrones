package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CadenaValidadores {
    private final ValidadorSolicitud primero;

    // Orden: de la verificacion mas barata a la mas cara (ver Decision de diseno 1 en el README)
    public CadenaValidadores(ValidadorDocumentosSoporte documentos,
                              ValidadorMontoMaximo monto,
                              ValidadorDuplicados duplicados) {
        documentos.siguiente(monto).siguiente(duplicados);
        this.primero = documentos;
    }

    public ResultadoValidacion validar(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientes) {
        return primero.validar(solicitud, pendientes);
    }
}
