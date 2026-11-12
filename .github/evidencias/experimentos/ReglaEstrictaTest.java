package com.empresa.reembolsos;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

// Experimento de la Decision 4: que pasaria si la Regla 1 prohibiera todo org.springframework
class ReglaEstrictaTest {

    @Test
    void procesamientoSinNadaDeSpring() {
        JavaClasses clases = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("com.empresa.reembolsos");

        noClasses().that().resideInAPackage("..procesamiento..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework..")
            .check(clases);
    }
}
