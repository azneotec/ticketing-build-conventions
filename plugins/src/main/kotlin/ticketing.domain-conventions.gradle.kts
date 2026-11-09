// Conventions for a hexagonal *domain* module: plain Java, zero framework dependencies.
plugins {
    id("ticketing.java-conventions")
}

/** Configure with: `domainDependencyGuard { allowed.add("org.jspecify") }` (state why). */
interface DomainDependencyGuardExtension {
    val allowed: SetProperty<String>
}

val guard = extensions.create<DomainDependencyGuardExtension>("domainDependencyGuard")
guard.allowed.convention(emptySet())

val checkDomainDependencies = tasks.register<CheckDomainDependencies>("checkDomainDependencies") {
    group = "verification"
    description = "Fails if the domain module has any non-allow-listed library on its main classpath."
    rootComponents.add(configurations.compileClasspath.flatMap { it.incoming.resolutionResult.rootComponent })
    rootComponents.add(configurations.runtimeClasspath.flatMap { it.incoming.resolutionResult.rootComponent })
    allowed = guard.allowed
}

// Fail fast, before compiling, so the error names the dependency rather than a compile error.
tasks.compileJava { dependsOn(checkDomainDependencies) }
tasks.check { dependsOn(checkDomainDependencies) }
