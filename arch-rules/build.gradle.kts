plugins {
    id("ticketing.java-conventions")
    `java-library`
    `maven-publish`
}

apply(from = rootProject.file("gradle/version.gradle.kts"))

dependencies {
    // api: services' tests use ArchUnit types (JavaClasses, ArchRule) through this library.
    api("com.tngtech.archunit:archunit:1.5.1")
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "ticketing-arch-rules"
            from(components["java"])
        }
    }
}
