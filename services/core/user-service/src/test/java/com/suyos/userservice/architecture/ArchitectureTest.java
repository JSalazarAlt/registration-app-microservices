package com.suyos.userservice.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Architecture tests using ArchUnit.
 *
 * <p>Validates architectural rules and package structure to ensure
 * consistent layering and dependency management.</p>
 */
class ArchitectureTest {
    
    /** Imported Java classes for architecture validation */
    private final JavaClasses classes = new ClassFileImporter()
        .importPackages("com.suyos.userservice");
    
    /**
     * Validates that controllers reside in controller package.
     */
    @Test
    void controllersShouldBeInControllerPackage() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Controller")
            .should().resideInAPackage("..controller..")
            .because("Controllers should be in controller package");
        
        rule.check(classes);
    }
    
    /**
     * Validates that services reside in service package.
     */
    @Test
    void servicesShouldBeInServicePackage() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Service")
            .should().resideInAPackage("..service..")
            .because("Services should be in service package");
        
        rule.check(classes);
    }
    
    /**
     * Validates that repositories reside in repository package.
     */
    @Test
    void repositoriesShouldBeInRepositoryPackage() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Repository")
            .should().resideInAPackage("..repository..")
            .because("Repositories should be in repository package");
        
        rule.check(classes);
    }
    
    /**
     * Validates that controllers do not directly depend on repositories.
     */
    @Test
    void controllersShouldNotDependOnRepositories() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..controller..")
            .should().dependOnClassesThat().resideInAPackage("..repository..")
            .because("Controllers should not directly access repositories");
        
        rule.check(classes);
    }
    
    /**
     * Validates that services do not depend on controllers.
     */
    @Test
    void servicesShouldNotDependOnControllers() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..service..")
            .should().dependOnClassesThat().resideInAPackage("..controller..")
            .because("Services should not depend on controllers");
        
        rule.check(classes);
    }
}
