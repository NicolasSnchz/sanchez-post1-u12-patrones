package com.empresa.reembolsos;

import com.empresa.reembolsos.procesamiento.calculo.CalculadoraReembolso;
import com.empresa.reembolsos.procesamiento.notificacion.ObservadorCambioEstado;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

// Las 4 reglas exactamente como las da la guia, para comprobar si pasan con el codigo del enunciado
@AnalyzeClasses(packages = "com.empresa.reembolsos")
public class ReglasArquitectura {

    @ArchTest
    static final ArchRule procesamientoSinPersistenciaNiWeb = noClasses()
        .that().resideInAPackage("..procesamiento..")
        .should().dependOnClassesThat()
        .resideInAnyPackage(
            "..infraestructura..", "..adaptadores..",
            "org.springframework.data..", "jakarta.persistence..");

    @ArchTest
    static final ArchRule controladorSoloUsaServicio = classes()
        .that().resideInAPackage("..adaptadores.rest..")
        .should().onlyAccessClassesThat()
        .resideInAnyPackage(
            "..aplicacion..", "..dominio..",
            "org.springframework.web..", "org.springframework.http..", "java..");

    @ArchTest
    static final ArchRule calculadorasImplementanInterfaz = classes()
        .that().resideInAPackage("..procesamiento.calculo..")
        .and().areNotInterfaces()
        .should().implement(CalculadoraReembolso.class);

    @ArchTest
    static final ArchRule notificadoresImplementanInterfaz = classes()
        .that().resideInAPackage("..procesamiento.notificacion..")
        .and().areNotInterfaces()
        .should().implement(ObservadorCambioEstado.class);
}
