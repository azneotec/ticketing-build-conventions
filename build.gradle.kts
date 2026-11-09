// Root of the repo: forwards lifecycle tasks to the included plugins build,
// so `./gradlew build publishToMavenLocal` covers plugins and arch-rules together.
plugins {
    base
}

val plugins = gradle.includedBuild("plugins")

tasks.named("build") { dependsOn(plugins.task(":build")) }
tasks.named("check") { dependsOn(plugins.task(":check")) }
tasks.named("clean") { dependsOn(plugins.task(":clean")) }

tasks.register("publishToMavenLocal") {
    group = "publishing"
    description = "Publishes the convention plugins and ticketing-arch-rules to ~/.m2."
    dependsOn(plugins.task(":publishToMavenLocal"))
}
