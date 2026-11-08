package com.example.chassis.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.example.chassis", importOptions = {ImportOption.DoNotIncludeTests.class})
public class HexagonalArchitectureTest {
    // 1. O Domínio deve ser Java Puro e não pode depender de Frameworks (Spring, JPA, Jackson, etc.)
    @ArchTest
    public static final ArchRule domain_must_be_framework_free =
            noClasses().
                    that().
                    resideInAPackage("..domain..").
                    should().
                    dependOnClassesThat().
                    resideInAnyPackage("..application..", "..infrastructure..").
                    because("As regras de negócio do domínio não devem ter visibilidade dos casos de uso ou dos adaptadores.");

    // 3. A camada de Aplicação (Casos de Uso / Ports) não pode depender da Infraestrutura
    @ArchTest
    public static final ArchRule application_should_not_depend_on_infrastructure =
            noClasses().
                    that().
                    resideInAPackage("..application..").
                    should().
                    dependOnClassesThat().
                    resideInAPackage("..infrastructure..").
                    because("Casos de uso e portas devem depender apenas do domínio, nunca de adaptadores concretos.");

    // 4. As classes do Domínio não podem usar anotações do Spring (@Service, @Component, @Repository)
    @ArchTest
    public static final ArchRule domain_classes_should_not_be_annotated_with_spring =
            noClasses().
                    that().
                    resideInAPackage("..domain..").
                    should().
                    beAnnotatedWith("org.springframework.stereotype.Component").
                    orShould().
                    beAnnotatedWith("org.springframework.stereotype.Service").
                    orShould().
                    beAnnotatedWith("org.springframework.stereotype.Repository").
                    because("Injeção de dependência e gerenciamento de beans do Spring devem ocorrer apenas na camada de infraestrutura.");

    // 5. Garantir que os adaptadores de infraestrutura fiquem restritos ao seu próprio pacote
    @ArchTest
    public static final ArchRule infrastructure_adapters_must_reside_in_infrastructure_package =
            classes().
                    that().
                    haveSimpleNameEndingWith("Adapter").
                    or().
                    haveSimpleNameEndingWith("Controller").
                    or().
                    haveSimpleNameEndingWith("Repository").
                    should().
                    resideInAPackage("..infrastructure..").
                    because("Adaptadores de entrada e saída devem ser organizados estritamente dentro do pacote de infraestrutura.");
}