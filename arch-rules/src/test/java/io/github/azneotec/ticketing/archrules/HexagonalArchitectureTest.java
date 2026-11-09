package io.github.azneotec.ticketing.archrules;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class HexagonalArchitectureTest {

  private static final String FIXTURES = "io.github.azneotec.ticketing.archrules.fixtures";

  /** Fixtures live in test sources, so import them explicitly (check() skips tests). */
  private static void check(HexagonalArchitecture rules, String fixture) {
    JavaClasses classes = new ClassFileImporter().importPackages(FIXTURES + "." + fixture);
    rules.check(classes);
  }

  private static HexagonalArchitecture rulesFor(String fixture) {
    return HexagonalArchitecture.forBasePackage(FIXTURES + "." + fixture);
  }

  @Test
  void acceptsAProperHexagon() {
    assertThatCode(() -> check(rulesFor("good"), "good")).doesNotThrowAnyException();
  }

  @Test
  void rejectsFrameworkInDomain() {
    assertThatThrownBy(() -> check(rulesFor("framework"), "framework"))
        .isInstanceOf(AssertionError.class)
        .hasMessageContaining("domain should not use Spring, Jakarta EE or JPA");
  }

  @Test
  void rejectsLibraryInDomain() {
    assertThatThrownBy(() -> check(rulesFor("library"), "library"))
        .isInstanceOf(AssertionError.class)
        .hasMessageContaining("domain should depend only on itself and the JDK");
  }

  @Test
  void allowsExplicitlyAllowedCorePackages() {
    HexagonalArchitecture rules = rulesFor("library").allowingCoreDependenciesOn("com.tngtech..");
    assertThatCode(() -> check(rules, "library")).doesNotThrowAnyException();
  }

  @Test
  void rejectsLibraryInApplication() {
    assertThatThrownBy(() -> check(rulesFor("appleak"), "appleak"))
        .isInstanceOf(AssertionError.class)
        .hasMessageContaining("application should depend only on itself, the domain and the JDK");
  }

  @Test
  void rejectsCoreDependingOnAdapter() {
    assertThatThrownBy(() -> check(rulesFor("outward"), "outward"))
        .isInstanceOf(AssertionError.class)
        .hasMessageContaining("domain and application should not depend on adapters");
  }

  @Test
  void rejectsAdapterDependingOnAnotherAdapter() {
    assertThatThrownBy(() -> check(rulesFor("sideways"), "sideways"))
        .isInstanceOf(AssertionError.class)
        .hasMessageContaining("adapters should not depend on each other");
  }

  @Test
  void checkWithoutArgumentsImportsProductionClassesOnly() {
    // Fixtures are test classes, so nothing is imported and the domain rule fails on
    // "no classes": a guard against a mistyped base package.
    assertThatThrownBy(() -> rulesFor("good").check())
        .isInstanceOf(AssertionError.class)
        .hasMessageContaining("failed to check any classes");
  }
}
