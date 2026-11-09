// Conventions for a Spring Boot application module (the module that holds the adapters and main()).
//
// Every service must set in application.properties/yaml:
//   spring.threads.virtual.enabled=true
// so request handling runs on virtual threads (cheap blocking, small heap).
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    id("ticketing.java-conventions")
    id("org.springframework.boot")
}

dependencies {
    // The Spring Boot BOM pins versions of every Spring/Boot-managed library.
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    testImplementation(platform(SpringBootPlugin.BOM_COORDINATES))
}

tasks.named<BootRun>("bootRun") {
    // Small heap: 16 GB laptop budget (plan: Spring services -Xmx256m..384m).
    jvmArgs("-Xmx256m")
}
