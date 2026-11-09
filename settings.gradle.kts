rootProject.name = "ticketing-build-conventions"

// The convention plugins are their own build; including it lets this build use them
// (arch-rules is built with ticketing.java-conventions) and lets one command build both.
includeBuild("plugins")

include("arch-rules")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
