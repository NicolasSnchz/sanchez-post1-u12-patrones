package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class ValidadorMontoMaximo extends ValidadorSolicitud {
    private static final BigDecimal TOPE_DIARIO_VIAJE = new BigDecimal("150000");

    // En viajes el tope es diario (RF2), asi que el tope de la solicitud depende de sus dias
    private static final Map<CategoriaGasto, Function<SolicitudReembolso, BigDecimal>> TOPES = Map.of(
        CategoriaGasto.VIAJE, s -> TOPE_DIARIO_VIAJE.multiply(BigDecimal.valueOf(s.getDiasViaje())),
        CategoriaGasto.REPRESENTACION, s -> new BigDecimal("2000000"),
        CategoriaGasto.CAPACITACION, s -> new BigDecimal("3000000"));

    @Override
    protected ResultadoValidacion validarPropio(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientes) {
        var tope = TOPES.get(solicitud.getCategoria()).apply(solicitud);
        return solicitud.getMontoSolicitado().compareTo(tope) > 0
            ? ResultadoValidacion.rechazar("Monto excede el tope de la categoria " + solicitud.getCategoria())
            : ResultadoValidacion.ok();
    }
}
