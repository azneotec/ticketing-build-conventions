// A separate build that consumes the conventions exactly like a service will:
// from mavenLocal, by plugin id and version.
pluginManagement {
    val ticketingConventionsVersion = providers.gradleProperty("ticketingConventionsVersion").get()
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
    plugins {
        id("ticketing.java-conventions") version ticketingConventionsVersion
        id("ticketing.domain-conventions") version ticketingConventionsVersion
        id("ticketing.spring-boot-conventions") version ticketingConventionsVersion
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal() // ticketing-arch-rules
        mavenCentral()
    }
}

rootProject.name = "hexagonal-sample"

include("domain", "application", "adapter")
