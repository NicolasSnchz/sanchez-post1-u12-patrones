package com.empresa.reembolsos.aplicacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import com.empresa.reembolsos.infraestructura.RepositorioSolicitudesJpa;
import com.empresa.reembolsos.procesamiento.calculo.CalculadoraCapacitacion;
import com.empresa.reembolsos.procesamiento.calculo.CalculadoraRepresentacion;
import com.empresa.reembolsos.procesamiento.calculo.CalculadoraViaje;
import com.empresa.reembolsos.procesamiento.calculo.SelectorCalculadora;
import com.empresa.reembolsos.procesamiento.validacion.CadenaValidadores;
import com.empresa.reembolsos.procesamiento.validacion.ValidadorDocumentosSoporte;
import com.empresa.reembolsos.procesamiento.validacion.ValidadorDuplicados;
import com.empresa.reembolsos.procesamiento.validacion.ValidadorMontoMaximo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.empresa.reembolsos.Solicitudes.solicitud;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitudReembolsoServiceTest {

    // Mock del repositorio: JpaRepository tiene decenas de metodos y aqui solo importan tres
    @Mock
    private RepositorioSolicitudesJpa repositorio;

    // Spy escrito a mano: un observador que anota cada aviso que recibe
    private final List<String> avisos = new ArrayList<>();

    private SolicitudReembolsoService servicio;

    @BeforeEach
    void setUp() {
        servicio = new SolicitudReembolsoService(
            new CadenaValidadores(new ValidadorDocumentosSoporte(), new ValidadorMontoMaximo(), new ValidadorDuplicados()),
            new SelectorCalculadora(List.of(new CalculadoraViaje(), new CalculadoraRepresentacion(), new CalculadoraCapacitacion())),
            List.of((solicitud, anterior) -> avisos.add(anterior + " -> " + solicitud.getEstado())),
            repositorio);
    }

    @Test
    void crear_solicitudValida_calculaElMontoYQuedaPendiente() {
        when(repositorio.findByEmpleadoIdAndEstado("E-100", EstadoSolicitud.PENDIENTE)).thenReturn(List.of());
        when(repositorio.save(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        SolicitudReembolso creada = servicio.crear(solicitud(CategoriaGasto.CAPACITACION, "1000000"));

        assertEquals(0, new BigDecimal("800000").compareTo(creada.getMontoAprobado()));
        assertEquals(EstadoSolicitud.PENDIENTE, creada.getEstado());
    }

    @Test
    void crear_conIdEnviadoPorElCliente_loDescartaParaNoSobrescribirOtraSolicitud() {
        when(repositorio.findByEmpleadoIdAndEstado("E-100", EstadoSolicitud.PENDIENTE)).thenReturn(List.of());
        when(repositorio.save(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
        SolicitudReembolso conId = solicitud(CategoriaGasto.VIAJE, "100000");
        conId.setId(42L);

        assertNull(servicio.crear(conId).getId());
    }

    @Test
    void crear_solicitudInvalida_lanzaExcepcionConElMotivoYNoLaGuarda() {
        when(repositorio.findByEmpleadoIdAndEstado("E-100", EstadoSolicitud.PENDIENTE)).thenReturn(List.of());
        SolicitudReembolso sinDocumentos = solicitud(CategoriaGasto.VIAJE, "100000");
        sinDocumentos.setDocumentosSoporte(false);

        SolicitudInvalidaException error = assertThrows(SolicitudInvalidaException.class,
            () -> servicio.crear(sinDocumentos));

        assertEquals("Faltan documentos soporte", error.getMessage());
        verify(repositorio, never()).save(any());
    }

    @Test
    void cambiarEstado_avisaALosObservadoresConElEstadoAnterior() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(pendiente()));
        when(repositorio.save(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        SolicitudReembolso aprobada = servicio.cambiarEstado(1L, EstadoSolicitud.APROBADA);

        assertEquals(EstadoSolicitud.APROBADA, aprobada.getEstado());
        assertEquals(List.of("PENDIENTE -> APROBADA"), avisos);
    }

    @Test
    void cambiarEstado_alMismoEstado_noGuardaNiAvisa() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(pendiente()));

        servicio.cambiarEstado(1L, EstadoSolicitud.PENDIENTE);

        assertTrue(avisos.isEmpty());
        verify(repositorio, never()).save(any());
    }

    @Test
    void cambiarEstado_solicitudInexistente_lanzaSolicitudNoEncontrada() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        SolicitudNoEncontradaException error = assertThrows(SolicitudNoEncontradaException.class,
            () -> servicio.cambiarEstado(99L, EstadoSolicitud.APROBADA));
        assertEquals("Solicitud no encontrada: 99", error.getMessage());
    }

    private SolicitudReembolso pendiente() {
        SolicitudReembolso pendiente = solicitud(CategoriaGasto.REPRESENTACION, "500000");
        pendiente.setId(1L);
        pendiente.setEstado(EstadoSolicitud.PENDIENTE);
        return pendiente;
    }
}
