package io.github.azneotec.ticketing.archrules;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Strict hexagonal layering rules for a ticketing service.
 *
 * <p>Expected packages under the service's base package:
 *
 * <ul>
 *   <li>{@code <base>.domain..} — plain Java; depends on nothing outside itself (and {@code
 *       java..})
 *   <li>{@code <base>.application..} — use cases and ports; depends only on domain
 *   <li>{@code <base>.adapter.<name>..} — e.g. {@code adapter.web}, {@code adapter.persistence};
 *       depends inward, never on another adapter
 * </ul>
 *
 * <p>One-line use from a JUnit test in the service:
 *
 * <pre>{@code
 * @Test
 * void isHexagonal() {
 *   HexagonalArchitecture.forBasePackage("io.github.azneotec.ticketing.catalogue").check();
 * }
 * }</pre>
 */
public final class HexagonalArchitecture {

  private static final String[] FRAMEWORK_PACKAGES = {
    "org.springframework..", "jakarta..", "javax.persistence..", "org.hibernate.."
  };

  private final String basePackage;
  private final List<String> extraCorePackages;

  private HexagonalArchitecture(String basePackage, List<String> extraCorePackages) {
    this.basePackage = Objects.requireNonNull(basePackage, "basePackage");
    this.extraCorePackages = List.copyOf(extraCorePackages);
  }

  /** Rules for the service whose code lives under {@code basePackage}. */
  public static HexagonalArchitecture forBasePackage(String basePackage) {
    return new HexagonalArchitecture(basePackage, List.of());
  }

  /**
   * Also lets domain and application code use these packages (ArchUnit syntax, e.g. {@code
   * "org.jspecify.."}). Use only for small, framework-free libraries, and say why in the test.
   */
  public HexagonalArchitecture allowingCoreDependenciesOn(String... packageIdentifiers) {
    List<String> extra = new ArrayList<>(extraCorePackages);
    extra.addAll(Arrays.asList(packageIdentifiers));
    return new HexagonalArchitecture(basePackage, extra);
  }

  /** Imports the production classes under the base package and checks all rules. */
  public void check() {
    check(
        new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(basePackage));
  }

  /** Checks all rules against already imported classes; reports every violation at once. */
  public void check(JavaClasses classes) {
    CompositeArchRule.of(rules()).check(classes);
  }

  /** All rules, for callers that want to check them one by one. */
  public List<ArchRule> rules() {
    return List.of(
        domainDependsOnlyOnItself(),
        noFrameworkInDomain(),
        applicationDependsOnlyOnDomain(),
        coreDoesNotDependOnAdapters(),
        adaptersDoNotDependOnEachOther());
  }

  /** Domain classes use only the domain, the JDK and explicitly allowed packages. */
  public ArchRule domainDependsOnlyOnItself() {
    return classes()
        .that()
        .resideInAPackage(domain())
        .should()
        .onlyDependOnClassesThat()
        .resideInAnyPackage(allowed(domain()))
        .as("domain should depend only on itself and the JDK")
        .because("the domain is the framework-free core of a hexagonal service");
  }

  /** Explicit guard with a clear message: no Spring, Jakarta EE or JPA in the domain. */
  public ArchRule noFrameworkInDomain() {
    return noClasses()
        .that()
        .resideInAPackage(domain())
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(FRAMEWORK_PACKAGES)
        .as("domain should not use Spring, Jakarta EE or JPA")
        .because("frameworks belong in adapters");
  }

  /** Application (use cases, ports) uses only itself, the domain and the JDK. */
  public ArchRule applicationDependsOnlyOnDomain() {
    return classes()
        .that()
        .resideInAPackage(application())
        .should()
        .onlyDependOnClassesThat()
        .resideInAnyPackage(allowed(application(), domain()))
        .as("application should depend only on itself, the domain and the JDK")
        .because("use cases talk to the outside world only through ports")
        .allowEmptyShould(true);
  }

  /** Dependencies point inward: domain and application never see an adapter. */
  public ArchRule coreDoesNotDependOnAdapters() {
    return noClasses()
        .that()
        .resideInAnyPackage(domain(), application())
        .should()
        .dependOnClassesThat()
        .resideInAPackage(adapter())
        .as("domain and application should not depend on adapters")
        .because("dependencies in a hexagonal service point inward");
  }

  /** Adapters are independent of each other (web must not call persistence directly). */
  public ArchRule adaptersDoNotDependOnEachOther() {
    return slices()
        .matching(basePackage + ".adapter.(*)..")
        .should()
        .notDependOnEachOther()
        .as("adapters should not depend on each other")
        .because("adapters talk to each other only through application ports")
        .allowEmptyShould(true);
  }

  private String domain() {
    return basePackage + ".domain..";
  }

  private String application() {
    return basePackage + ".application..";
  }

  private String adapter() {
    return basePackage + ".adapter..";
  }

  private String[] allowed(String... ownPackages) {
    List<String> packages = new ArrayList<>(Arrays.asList(ownPackages));
    packages.add("java..");
    packages.addAll(extraCorePackages);
    return packages.toArray(String[]::new);
  }
}
