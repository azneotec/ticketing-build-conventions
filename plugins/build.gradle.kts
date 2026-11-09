plugins {
    // Compiles src/main/kotlin/*.gradle.kts into real plugins ("precompiled script plugins").
    `kotlin-dsl`
    `maven-publish`
}

apply(from = "../gradle/version.gradle.kts")

dependencies {
    // Third-party Gradle plugins that our convention plugins apply.
    implementation("com.diffplug.spotless:spotless-plugin-gradle:8.11.0")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:4.1.2")
}
