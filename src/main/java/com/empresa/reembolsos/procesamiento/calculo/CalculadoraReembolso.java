package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

import java.math.BigDecimal;

/**
 * Estrategia de calculo del monto aprobado para una categoria de gasto. Cada categoria tiene su
 * propia implementacion; agregar una categoria es agregar una clase, sin tocar las existentes.
 */
public interface CalculadoraReembolso {

    /** La categoria cuyo monto calcula esta estrategia; el selector la usa como llave. */
    CategoriaGasto getCategoria();

    /** El monto que se aprueba para la solicitud, segun la formula de la categoria. */
    BigDecimal calcular(SolicitudReembolso solicitud);
}
