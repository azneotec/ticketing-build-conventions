# ticketing-build-conventions

Shared Gradle build rules for every Java service of the **Distributed Ticketing Platform**. A service applies one plugin and gets the Java 25 toolchain, strict compiler settings, JUnit 5, an 80% coverage gate, enforced formatting and (for domain modules) a guard against framework dependencies. Decisions: [ADR-004](https://github.com/azneotec/ticketing-docs/blob/master/decisions/ADR-004-strict-hexagonal-archunit.md) and [ADR-029](https://github.com/azneotec/ticketing-docs/blob/master/decisions/ADR-029-spotless-google-java-format.md).

## What's inside

| Path | Artifact | What it is |
|---|---|---|
| `plugins/` | `io.github.azneotec.ticketing:ticketing-gradle-plugins` | Included build with three precompiled script plugins (below) |
| `arch-rules/` | `io.github.azneotec.ticketing:ticketing-arch-rules` | ArchUnit rules for strict hexagonal layering |
| `samples/hexagonal-sample/` | — | Separate build that consumes both, like a real service |
| `samples/verify-failure-cases.sh` | — | Proves each guard fails the build when violated |

### Plugins

| Plugin id | Use it for | Gives you |
|---|---|---|
| `ticketing.java-conventions` | Any Java module | Java 25 toolchain; `-parameters -Xlint:all -Werror`; JUnit 5 + AssertJ; JaCoCo report + **80% line coverage** gate; **Spotless** with google-java-format. Coverage and formatting checks run in `check`, so in `build` too |
| `ticketing.domain-conventions` | The `domain` module | Everything above + `checkDomainDependencies`: fails if **any** external library is on the main compile or runtime classpath, unless allow-listed |
| `ticketing.spring-boot-conventions` | The Spring Boot app module (adapters + `main`) | Everything in java-conventions + Spring Boot plugin, Spring Boot BOM, `bootRun` with `-Xmx256m` |

Versions (Spring Boot 4.1.2, JUnit 6.1.3, JaCoCo 0.8.16, google-java-format 2.0.0, Spotless 8.11.0, ArchUnit 1.5.1) are pinned in `plugins/src/main/kotlin/TicketingVersions.kt` and `plugins/build.gradle.kts`.

## Using it in a service

`settings.gradle.kts`:
```kotlin
pluginManagement {
    val ticketingConventionsVersion = providers.gradleProperty("ticketingConventionsVersion").get()
    repositories {
        mavenLocal()          // temporary, until we publish to a real repository
        gradlePluginPortal()
    }
    plugins {
        id("ticketing.java-conventions") version ticketingConventionsVersion
        id("ticketing.domain-conventions") version ticketingConventionsVersion
        id("ticketing.spring-boot-conventions") version ticketingConventionsVersion
    }
}

// The plugins declare no repositories; each build declares them once, here.
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}
```
`gradle.properties`: `ticketingConventionsVersion=0.1.0`

Module build files:
```kotlin
plugins { id("ticketing.domain-conventions") }        // domain
plugins { id("ticketing.java-conventions") }          // application
plugins { id("ticketing.spring-boot-conventions") }   // adapters + main()
```

Every Spring Boot service **must** set in `application.properties`:
```properties
spring.threads.virtual.enabled=true
```

### Allowing a library in the domain
Only small, framework-free libraries, with a reason:
```kotlin
domainDependencyGuard {
    allowed.add("org.jspecify")              // whole group, or "group:name" for one module
}
```

### Architecture test (one line)
```kotlin
testImplementation("io.github.azneotec.ticketing:ticketing-arch-rules:$ticketingConventionsVersion")
```
```java
@Test
void isHexagonal() {
  HexagonalArchitecture.forBasePackage("io.github.azneotec.ticketing.catalogue").check();
}
```
Put it in the module whose test classpath sees all layers (the Spring Boot module). Expected packages: `<base>.domain..`, `<base>.application..`, `<base>.adapter.<name>..`.

Rules:
- domain depends only on itself and `java..`
- no Spring, Jakarta EE or JPA in the domain
- application depends only on itself, the domain and `java..`
- domain and application never depend on adapters
- adapters do not depend on each other

## Build, test, publish
```bash
./gradlew build publishToMavenLocal          # plugins + arch-rules
./gradlew -p samples/hexagonal-sample build  # sample consumes them from ~/.m2
samples/verify-failure-cases.sh              # baseline passes, every violation fails
./gradlew spotlessApply                      # fix formatting
./gradlew clean && ./gradlew -p plugins clean # full clean (two runs: see build.gradle.kts)
```

## Versioning and releasing
The version comes from Git tags, set by hand (`gradle/version.gradle.kts`):

| State of `HEAD` | Version |
|---|---|
| exactly on tag `vX.Y.Z` | `X.Y.Z` |
| commits after `vX.Y.Z` | `X.Y.(Z+1)-SNAPSHOT` |
| no tag yet | `0.1.0-SNAPSHOT` |

To release (semantic versioning: breaking change → major, new feature → minor, fix → patch):
1. Make sure `master` is clean and `./gradlew build` and `samples/verify-failure-cases.sh` pass.
2. Set `ticketingConventionsVersion` in `samples/hexagonal-sample/gradle.properties` to the new version and commit (`chore: release X.Y.Z`).
3. `git tag -a vX.Y.Z -m "vX.Y.Z"`
4. `./gradlew build publishToMavenLocal`, then `samples/verify-failure-cases.sh`.
5. `git push && git push origin vX.Y.Z`
6. Afterwards, set the sample back to `X.Y.(Z+1)-SNAPSHOT` for development.

Services then bump `ticketingConventionsVersion` in their own `gradle.properties`.

## Requirements
JDK 25 (Gradle toolchains find it; the wrapper downloads Gradle 9.9.0). Commits follow [Conventional Commits](https://www.conventionalcommits.org/); trunk-based on `master`.
