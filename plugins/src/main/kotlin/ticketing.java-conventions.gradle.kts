// Base conventions for every Java module in the ticketing platform.
plugins {
    java
    jacoco
    id("com.diffplug.spotless")
}

// Repositories are declared once per build in settings.gradle.kts
// (dependencyResolutionManagement), not here.

java {
    // Gradle finds (or provisions) a JDK 25 for compile/test, whatever JDK runs Gradle itself.
    toolchain { languageVersion = JavaLanguageVersion.of(TicketingVersions.JAVA) }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    // -parameters: keep parameter names in bytecode (Spring, Jackson rely on it).
    // -Xlint:all + -Werror: every compiler warning fails the build. Relax a lint
    // only in the module that needs it, with a comment that says why.
    options.compilerArgs.addAll(listOf("-parameters", "-Xlint:all", "-Werror"))
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:${TicketingVersions.JUNIT}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:${TicketingVersions.ASSERTJ}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

jacoco { toolVersion = TicketingVersions.JACOCO }

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = TicketingVersions.MIN_LINE_COVERAGE.toBigDecimal()
            }
        }
    }
    // JaCoCo skips this task when no test wrote coverage data. That would let a module
    // with production code but no tests pass the gate, so run it whenever main has code
    // (no data = 0% = failure).
    val mainJava = sourceSets.main.get().allJava
    setOnlyIf { !mainJava.isEmpty }
}

tasks.check { dependsOn(tasks.jacocoTestCoverageVerification) }

spotless {
    java {
        googleJavaFormat(TicketingVersions.GOOGLE_JAVA_FORMAT)
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
